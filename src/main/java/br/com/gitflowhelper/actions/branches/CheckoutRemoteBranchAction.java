package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.util.ActionParamsService;
import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import git4idea.GitBranch;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public class CheckoutRemoteBranchAction extends BaseAction {

    public CheckoutRemoteBranchAction(
            String remoteBranchName,
            boolean isCurrent
    ) {
        super(remoteBranchName.replaceAll("_", "__"),
                "Checkout remote branch "+remoteBranchName,
                (isCurrent ?
                        AllIcons.Gutter.Bookmark :
                        remoteBranchName.equals(BaseAction.REMOTE+"/"+GitFlowSettingsService.getInstance(ActionParamsService.getProject()).getMainBranch()) ?
                                AllIcons.Nodes.Favorite :
                                AllIcons.Vcs.BranchNode
                ));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        //e.getPresentation().setEnabled(!isCurrent);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProject();
        GitRepository repository = ActionParamsService.getRepo(this);
        String currentBranchName = repository.getCurrentBranchName();
        String checkoutBranchName = getTemplatePresentation().getText().replaceAll("__", "_");
        boolean isCurrent = currentBranchName.equals(checkoutBranchName);

        if (isCurrent) return;

        GitExecutor executor = new GitExecutor(project);

        String localBranch =
                checkoutBranchName.substring(checkoutBranchName.indexOf('/') + 1);

        GitBranch branch = repository.getBranches().findLocalBranch(localBranch);
        if (branch != null) {
            new CheckoutLocalBranchAction(localBranch, false)
                    .checkout(repository, project, localBranch);
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
                        checkoutBranchName
                );
                repository.update();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Remote branch "+checkoutBranchName+" checked out successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });

        //VirtualFileManager.getInstance().asyncRefresh(null);
    }
}
