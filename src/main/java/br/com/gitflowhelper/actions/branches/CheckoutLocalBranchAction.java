package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.util.ActionParamsService;
import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public class CheckoutLocalBranchAction extends BaseAction {

    public CheckoutLocalBranchAction(
            String label,
            String localBranchName
    ) {
        //cheating intellij
        super(label,
                GitFlowDescriptions.CHECKOUT_LOCAL.getValue() + localBranchName,
                AllIcons.Actions.CheckOut);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        GitRepository repo = ActionParamsService.getRepo(this);
        String remoteBranchName = ActionParamsService.getName(this);

        if (repo.getCurrentBranch() != null) {
            String current = repo.getCurrentBranch().getName();
            if (remoteBranchName.equals(current)) {
                e.getPresentation().setEnabled(false);
            }
        }

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProject();
        GitRepository repository = ActionParamsService.getRepo(this);
        String currentBranchName = repository.getCurrentBranchName();
        String checkoutBranchName = ActionParamsService.getName(this);
        boolean isCurrent = currentBranchName.equals(checkoutBranchName);

        if (isCurrent) return;

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                checkout(repository, project, checkoutBranchName);
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Local branch "+checkoutBranchName+" checked out successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });
        //VirtualFileManager.getInstance().asyncRefresh(null);
    }

    //can be called from outside
    public void checkout(GitRepository repository, Project project, String checkoutBranchName) {
        GitExecutor executor = new GitExecutor(project);
        executor.execute(
                repository.getRoot(),
                GitCommand.CHECKOUT,
                checkoutBranchName
        );
        repository.update();
    }
}
