package br.com.gitflowhelper.popup;

import br.com.gitflowhelper.actions.ActionBuilder;
import br.com.gitflowhelper.util.ActionParamsService;
import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.actions.InitAction;
import br.com.gitflowhelper.actions.branches.CheckoutLocalBranchAction;
import br.com.gitflowhelper.actions.branches.CheckoutRemoteBranchAction;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.GitBranchUtils;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import br.com.gitflowhelper.util.PropertyObserver;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public final class GitFlowPopup extends PropertyObserver {
    private ListPopup listPopup;

    public GitFlowPopup() {
        Project project = ActionParamsService.getProject();

        DataManager.getInstance()
            .getDataContextFromFocusAsync()
            .onSuccess(dataContext -> {

                this.listPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                        "Git Flow",
                        createGroup(project),
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true
                );
                this.listPopup.setCaptionIcon(PluginIcons.GitFlow);

                addPropertyChangeListener(ActionParamsService.getInstance());

                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    String branchName = GitBranchUtils.getCurrentBranchName(project);
                    firePropertyChange("branchName", "", branchName);
                });

                this.listPopup.addListener(new JBPopupListener() {
                       @Override
                       public void beforeShown(@NotNull LightweightWindowEvent event) {
                           var oldPlace = listPopup.getLocationOnScreen();
                           var newPlace = new Point((int) oldPlace.getX(), (int) oldPlace.getY()+45);
                           listPopup.setLocation(newPlace);
                           JBPopupListener.super.beforeShown(event);
                       }
                   }
                );
            });
    }

    private DefaultActionGroup createGroup(Project project) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new InitAction("Init..."));
        //group.add(new OpenTreePopupAction("Tree"));
        group.addSeparator();

        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        for (GitRepository repository : repoManager.getRepositories()) {
            group.add(repositoryBranchGroup(repository, project));
        }

        group.addSeparator();
        group.add(flowGroup("Feature", AllIcons.Actions.AddFile, GitFlowDescriptions.FEATURE_GROUP.getValue()));
        group.add(flowGroup("Release", AllIcons.Nodes.UpFolder, GitFlowDescriptions.RELEASE_GROUP.getValue()));
        group.add(flowGroup("Hotfix", AllIcons.General.ExternalTools, GitFlowDescriptions.HOTFIX_GROUP.getValue()));
        return group;
    }

    public ListPopup getPopup() {
        return this.listPopup;
    }

    private DefaultActionGroup flowGroup(String type, Icon icon, String description) {
        DefaultActionGroup group = new DefaultActionGroup(type, description, icon);
        group.setPopup(true);
        group.add(flowAction(type, "start"));
        group.add(flowAction(type, "publish"));
        group.add(flowAction(type, "finish"));
        return group;
    }

    private AnAction flowAction(String type, String action) {
        String actionTitle = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);
        BaseAction act = ActionBuilder.createActionInstance(
                type+actionTitle+"Action",
                actionTitle);
        return act;
    }

    private DefaultActionGroup repositoryBranchGroup(GitRepository repository, Project project) {
        DefaultActionGroup group = new DefaultActionGroup(
                repository.getProject().getName(),
                GitFlowDescriptions.REPO_GROUP.getValue(),
                AllIcons.Actions.ProjectDirectory);
        group.setPopup(true);

        String currentBranch = repository.getCurrentBranchName();

        //--------------------------------------------------------------------------------------------

        group.add(new AnAction("Local Branches", "", AllIcons.Actions.MenuOpen) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(false);
                presentation.setWeight(Presentation.HIGHER_WEIGHT);
            }
        });
        group.addSeparator();

        //--------------------------------------------------------------------------------------------

        java.util.List<GitLocalBranch> orderedLocalBranches = sortBranches(
                repository.getBranches().getLocalBranches(),
                currentBranch,
                GitLocalBranch::getName,
                project
        );

        orderedLocalBranches.forEach(branch -> {
            boolean isCurrent = branch.getName().equals(currentBranch);
            DefaultActionGroup checkoutDeleteGr = new DefaultActionGroup(
                    branch.getName(),
                    GitFlowDescriptions.REPO_GROUP.getValue(),
                    (isCurrent ?
                            AllIcons.Gutter.Bookmark :
                            branch.getName().equals(GitFlowSettingsService.getInstance(ActionParamsService.getProject()).getMainBranch()) ?
                                    AllIcons.Nodes.Favorite :
                                    AllIcons.Vcs.BranchNode));
            checkoutDeleteGr.setPopup(true);
            var newCheckoutAction = new CheckoutLocalBranchAction(
                    "Checkout",
                    isCurrent
            );
            var newDeleteAction = new CheckoutLocalBranchAction(
                    "Delete",
                    isCurrent
            );
            checkoutDeleteGr.add(newCheckoutAction);
            checkoutDeleteGr.add(newDeleteAction);
            ActionParamsService.addRepo(newCheckoutAction, repository);
            ActionParamsService.addRepo(newDeleteAction, repository);
            group.add(checkoutDeleteGr);
        });

        //--------------------------------------------------------------------------------------------

        group.addSeparator();
        group.add(new AnAction("Remote Branches", "", AllIcons.Actions.MenuOpen) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(false);
                presentation.setWeight(Presentation.EVEN_HIGHER_WEIGHT);
            }
        });
        group.addSeparator();

        //--------------------------------------------------------------------------------------------

        java.util.List<GitRemoteBranch> orderedRemoteBranches = sortBranches(
                repository.getBranches().getRemoteBranches(),
                BaseAction.REMOTE+"/"+currentBranch,
                GitRemoteBranch::getName,
                project
        );
        orderedRemoteBranches.forEach(branch -> {
            boolean isCurrent = branch.getName().equals(BaseAction.REMOTE+"/"+currentBranch);
            DefaultActionGroup checkoutDeleteGr = new DefaultActionGroup(
                    branch.getName(),
                    GitFlowDescriptions.REPO_GROUP.getValue(),
                    (isCurrent ?
                            AllIcons.Gutter.Bookmark :
                            branch.getName().equals(BaseAction.REMOTE+"/"+GitFlowSettingsService.getInstance(ActionParamsService.getProject()).getMainBranch()) ?
                                    AllIcons.Nodes.Favorite :
                                    AllIcons.Vcs.BranchNode
                    ));
            checkoutDeleteGr.setPopup(true);
            var newCheckoutAction =new CheckoutRemoteBranchAction(
                    "Checkout",
                    isCurrent
            );
            var newDeleteAction =new CheckoutRemoteBranchAction(
                    "Delete",
                    isCurrent
            );
            checkoutDeleteGr.add(newCheckoutAction);
            checkoutDeleteGr.add(newDeleteAction);
            ActionParamsService.addRepo(newCheckoutAction, repository);
            ActionParamsService.addRepo(newDeleteAction, repository);
            group.add(checkoutDeleteGr);
        });

        //--------------------------------------------------------------------------------------------

        return group;
    }

    private <T> java.util.List<T> sortBranches(
            Collection<T> branches,
            String currentBranchName,
            Function<T, String> branchNameExtractor,
            Project project) {
        GitFlowSettingsService service = GitFlowSettingsService.getInstance(project);
        Map<String, T> byName = new HashMap<>();
        for (T branch : branches) {
            byName.put(branchNameExtractor.apply(branch), branch);
        }

        List<T> result = new ArrayList<>();

        // 1) current branch
        if (currentBranchName != null && byName.containsKey(currentBranchName)) {
            result.add(byName.remove(currentBranchName));
        }

        // 2) main
        if (byName.containsKey(service.getMainBranch())) {
            result.add(byName.remove(service.getMainBranch()));
        }
        if (byName.containsKey(BaseAction.REMOTE + "/" + service.getMainBranch())) {
            result.add(byName.remove(BaseAction.REMOTE + "/" + service.getMainBranch()));
        }

        // 3) develop
        if (byName.containsKey(service.getDevelopBranch())) {
            result.add(byName.remove(service.getDevelopBranch()));
        }
        if (byName.containsKey(BaseAction.REMOTE + "/" + service.getDevelopBranch())) {
            result.add(byName.remove(BaseAction.REMOTE + "/" + service.getDevelopBranch()));
        }


        // 4) everything else
        byName.values().stream()
                .sorted(Comparator.comparing(branchNameExtractor, String.CASE_INSENSITIVE_ORDER))
                .forEach(result::add);

        return result;
    }

}
