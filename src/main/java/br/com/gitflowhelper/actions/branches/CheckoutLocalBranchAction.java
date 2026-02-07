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
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public class CheckoutLocalBranchAction extends BaseAction {

    private final GitRepository repository;

    public CheckoutLocalBranchAction(
            Project project,
            GitRepository repository,
            String localBranchName,
            boolean isCurrent
    ) {
        super(localBranchName,
                "Checkout local branch "+localBranchName,
                (isCurrent ?
                    AllIcons.Gutter.Bookmark :
                    localBranchName.equals(GitFlowSettingsService.getInstance(project).getMainBranch()) ?
                            AllIcons.Nodes.Favorite :
                            AllIcons.Vcs.BranchNode),
                project, localBranchName);
        this.repository = repository;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        //e.getPresentation().setEnabled(!isCurrent);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String currentBranchName = repository.getCurrentBranchName();
        String checkoutBranchName = getTemplatePresentation().getText();
        boolean isCurrent = currentBranchName.equals(checkoutBranchName);

        if (isCurrent) return;

        GitExecutor executor = new GitExecutor(project);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                executor.execute(
                        repository.getRoot(),
                        GitCommand.CHECKOUT,
                        checkoutBranchName
                );
                repository.update();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Local branch "+branchName+" checked out successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });
        //VirtualFileManager.getInstance().asyncRefresh(null);
    }
}
