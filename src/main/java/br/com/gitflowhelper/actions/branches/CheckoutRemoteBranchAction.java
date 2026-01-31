package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import git4idea.GitBranch;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public class CheckoutRemoteBranchAction extends BaseAction {

    private final Project project;
    private final GitRepository repository;
    private final String remoteBranch;
    private final boolean isCurrent;

    public CheckoutRemoteBranchAction(
            Project project,
            GitRepository repository,
            String remoteBranch,
            boolean isCurrent
    ) {

        super(project, remoteBranch, null, null,
                remoteBranch, (isCurrent ?
                        AllIcons.Gutter.Bookmark :
                        remoteBranch.equals(BaseAction.REMOTE+"/"+GitFlowSettingsService.getInstance(project).getMainBranch()) ?
                                AllIcons.Nodes.Favorite :
                                AllIcons.Vcs.BranchNode), "Checkout remote branch "+remoteBranch);
        this.project = project;
        this.repository = repository;
        this.remoteBranch = remoteBranch;
        this.isCurrent = isCurrent;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        //e.getPresentation().setEnabled(!isCurrent);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        if (isCurrent) return;

        GitExecutor executor = new GitExecutor(project);

        String localBranch =
                remoteBranch.substring(remoteBranch.indexOf('/') + 1);

        GitBranch branch = this.repository.getBranches().findLocalBranch(localBranch);
        if (branch != null) {
            new CheckoutLocalBranchAction(
                    this.project, this.repository, localBranch, false
            ).actionPerformed(e);
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                executor.execute(
                        repository.getRoot(),
                        GitCommand.CHECKOUT,
                        "-b",
                        localBranch,
                        "--track",
                        remoteBranch
                );
                repository.update();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Remote branch "+branchName+" checked out successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });

        //VirtualFileManager.getInstance().asyncRefresh(null);
    }
}
