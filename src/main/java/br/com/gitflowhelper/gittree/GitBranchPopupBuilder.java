package br.com.gitflowhelper.gittree;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.actions.branches.CheckoutLocalBranchAction;
import br.com.gitflowhelper.actions.branches.CheckoutRemoteBranchAction;
import br.com.gitflowhelper.actions.branches.DeleteLocalBranchAction;
import br.com.gitflowhelper.actions.branches.DeleteRemoteBranchAction;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.ActionParamsService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class GitBranchPopupBuilder {

    private static JBPopup jbPopup = null;

    public static JBPopup createPopup(Project project) {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
        DefaultMutableTreeNode localRoot = new DefaultMutableTreeNode("Local");
        DefaultMutableTreeNode remoteRoot = new DefaultMutableTreeNode("Remote");

        root.add(localRoot);
        root.add(remoteRoot);

        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repositories = manager.getRepositories();

        for (GitRepository repo : repositories) {
            groupLocalBranches(repo, localRoot, project);
            groupRemoteBranches(repo, remoteRoot, project);
        }

        Tree tree = new Tree(new DefaultTreeModel(root));
        tree.setRootVisible(false);

        installTreeCellRenderer(tree);
        installMouseListener(tree);
        installSpeedSearch(tree);

        int i = 0;
        while (i < tree.getRowCount()) {
            tree.expandRow(i);
            i++;
        }

        JBScrollPane pane = new JBScrollPane(tree);
        Dimension size = tree.getPreferredSize();
        int maxHeight = 400;
        int finalHeight = Math.min(size.height, maxHeight);
        pane.setPreferredSize(new Dimension(size.width + 25, finalHeight+27));

        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Git Branches", PluginIcons.GitFlow, SwingConstants.CENTER);
        title.setHorizontalTextPosition(SwingConstants.RIGHT);
        title.setBorder(JBUI.Borders.empty(8, 8));

        panel.add(title, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, tree)
                .setFocusable(true)
                .setRequestFocus(true)
                .setResizable(true)
                .createPopup();

        popup.addListener(new JBPopupListener() {
              @Override
              public void beforeShown(@NotNull LightweightWindowEvent event) {
                  StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                  Point locationOnScreen = statusBar.getComponent().getLocationOnScreen();
                  int x = locationOnScreen.x;
                  int y = locationOnScreen.y;
                  popup.setLocation(new Point(popup.getLocationOnScreen().x, y-popup.getSize().height));
              }
        });

        jbPopup = popup;
        return popup;
    }

    public static JBPopup getJbPopup() {
        return jbPopup;
    }

    private static void installMouseListener(Tree tree) {
        tree.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                int row = tree.getClosestRowForLocation(0, e.getY());

                if (row != -1) {
                    TreePath path = tree.getPathForRow(row);
                    DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) path.getLastPathComponent();

                    if (node.isLeaf()) {
                        tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        tree.setCursor(Cursor.getDefaultCursor());
                    }

                } else {
                    tree.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                TreePath path = getPathByY(e.getY());
                if (path == null) {
                    return;
                }

                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) path.getLastPathComponent();

                if (node.isLeaf()) {
                    showPopup(node, e);
                }
            }

            private TreePath getPathByY(int y) {
                int row = tree.getClosestRowForLocation(0, y);
                if (row < 0) return null;

                Rectangle r = tree.getRowBounds(row);
                if (r == null) return null;

                // VERY IMPORTANT: prevents “sticking” to the last item when clicking outside the rows
                if (y < r.y || y >= r.y + r.height) return null;

                return tree.getPathForRow(row);
            }

            private void showPopup(DefaultMutableTreeNode node, MouseEvent e) {
                BranchNode branchNode = (BranchNode) node.getUserObject();
                DefaultActionGroup group = branchNode.getGroup();
                ActionPopupMenu popupMenu = ActionManager.getInstance()
                                .createActionPopupMenu("MyTreePopup", group);
                popupMenu.getComponent().show(tree, tree.getX()+ tree.getWidth(), e.getY()-5);
            }
        });
    }

    private static void installTreeCellRenderer(Tree tree) {
        tree.setCellRenderer(new TreeCellRenderer() {

            private final JLabel chevron = new JLabel(AllIcons.General.ArrowRight);
            private final JPanel chevronWrapper = new JPanel(new BorderLayout());

            // wrapper com padding
            {
                chevronWrapper.setOpaque(false);
                chevronWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // space at right
                chevronWrapper.add(chevron, BorderLayout.CENTER);
            }

            private final ColoredTreeCellRenderer mainRenderer = new ColoredTreeCellRenderer() {
                @Override
                public void customizeCellRenderer(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    Object userObject = node.getUserObject();

                    if (userObject instanceof BranchNode branchNode) {
                        setIcon(branchNode.getIcon());
                        append(branchNode.getName());
                    } else {
                        append(userObject.toString(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                        setIcon(AllIcons.Nodes.Folder);
                    }
                }
            };

            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {

                JPanel panel = new JPanel(new BorderLayout());
                panel.removeAll();
                panel.setOpaque(false);

                mainRenderer.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);

                panel.add(mainRenderer, BorderLayout.CENTER);

                if (leaf) {
                    panel.add(chevronWrapper, BorderLayout.EAST);
                }

                return panel;
            }
        });

    }

    private static void installSpeedSearch(Tree tree) {
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree, path -> {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            return node.getUserObject().toString();
        }, true);
    }

    private static void groupLocalBranches(GitRepository repo, DefaultMutableTreeNode parent, Project project) {
        DefaultMutableTreeNode feature = new DefaultMutableTreeNode("feature");
        DefaultMutableTreeNode release = new DefaultMutableTreeNode("release");
        DefaultMutableTreeNode hotfix = new DefaultMutableTreeNode("hotfix");

        for (GitLocalBranch branch : repo.getBranches().getLocalBranches()) {
            boolean current = branch.getName().endsWith(repo.getCurrentBranchName());
            addBranchNode(branch.getName(), parent, feature, release, hotfix, false, repo, project, current);
        }

        attachIfNotEmpty(parent, feature);
        attachIfNotEmpty(parent, release);
        attachIfNotEmpty(parent, hotfix);
    }

    private static void groupRemoteBranches(GitRepository repo, DefaultMutableTreeNode parent, Project project) {
        DefaultMutableTreeNode feature = new DefaultMutableTreeNode("feature");
        DefaultMutableTreeNode release = new DefaultMutableTreeNode("release");
        DefaultMutableTreeNode hotfix = new DefaultMutableTreeNode("hotfix");

        for (GitRemoteBranch branch : repo.getBranches().getRemoteBranches()) {
            boolean current = branch.getName().endsWith(repo.getCurrentBranchName());
            addBranchNode(branch.getName(), parent, feature, release, hotfix, true, repo, project, current);
        }

        attachIfNotEmpty(parent, feature);
        attachIfNotEmpty(parent, release);
        attachIfNotEmpty(parent, hotfix);
    }

    private static void addBranchNode(String name,
                                      DefaultMutableTreeNode parent,
                                      DefaultMutableTreeNode feature,
                                      DefaultMutableTreeNode release,
                                      DefaultMutableTreeNode hotfix,
                                      Boolean remote,
                                      GitRepository repo,
                                      Project project,
                                      Boolean current) {

        DefaultActionGroup group = new DefaultActionGroup();
        Icon icon = null;
        String mainBranch = GitFlowSettingsService.getInstance(project).getMainBranch() != null  ?
                GitFlowSettingsService.getInstance(project).getMainBranch() : "";
        String featurePrefix = GitFlowSettingsService.getInstance(project).getFeaturePrefix() != null  ?
                GitFlowSettingsService.getInstance(project).getFeaturePrefix() : "";
        String releasePrefix = GitFlowSettingsService.getInstance(project).getReleasePrefix() != null  ?
                GitFlowSettingsService.getInstance(project).getReleasePrefix() : "";
        String hotfixPrefix = GitFlowSettingsService.getInstance(project).getHotfixPrefix() != null  ?
                GitFlowSettingsService.getInstance(project).getHotfixPrefix() : "";
        
        icon = (current ?
                AllIcons.Gutter.Bookmark :
                name.endsWith(mainBranch) ?
                        AllIcons.Nodes.Favorite :
                        AllIcons.Vcs.BranchNode);

        var node = new DefaultMutableTreeNode(
                new BranchNode(
                        name,
                        icon,
                        false,
                        remote,
                        group
                ));
        var branchNode = (BranchNode) node.getUserObject();

        if (branchNode.isRemote()) {
            var newCheckoutAction = new CheckoutRemoteBranchAction("Checkout", branchNode.getName());
            var newDeleteAction = new DeleteRemoteBranchAction("Delete", branchNode.getName());
            ActionParamsService.addRepo(newCheckoutAction, repo);
            ActionParamsService.addRepo(newDeleteAction, repo);
            ActionParamsService.addName(newCheckoutAction, branchNode.getName());
            ActionParamsService.addName(newDeleteAction, branchNode.getName());
            group.add(newCheckoutAction);
            group.add(newDeleteAction);
            if (name.startsWith(BaseAction.REMOTE+"/"+featurePrefix)) {
                feature.add(node);
            } else if (name.startsWith(BaseAction.REMOTE+"/"+releasePrefix)) {
                release.add(node);
            } else if (name.startsWith(BaseAction.REMOTE+"/"+hotfixPrefix)) {
                hotfix.add(node);
            } else {
                parent.add(node);
            }
        } else {
            var newCheckoutAction = new CheckoutLocalBranchAction("Checkout", branchNode.getName());
            var newDeleteAction = new DeleteLocalBranchAction("Delete", branchNode.getName());
            ActionParamsService.addRepo(newCheckoutAction, repo);
            ActionParamsService.addRepo(newDeleteAction, repo);
            ActionParamsService.addName(newCheckoutAction, branchNode.getName());
            ActionParamsService.addName(newDeleteAction, branchNode.getName());
            group.add(newCheckoutAction);
            group.add(newDeleteAction);
            if (name.startsWith(featurePrefix)) {
                feature.add(node);
            } else if (name.startsWith(releasePrefix)) {
                release.add(node);
            } else if (name.startsWith(hotfixPrefix)) {
                hotfix.add(node);
            } else {
                parent.add(node);
            }
        }

    }

    private static void attachIfNotEmpty(DefaultMutableTreeNode parent, DefaultMutableTreeNode node) {
        if (node.getChildCount() > 0) {
            parent.add(node);
        }
    }
}