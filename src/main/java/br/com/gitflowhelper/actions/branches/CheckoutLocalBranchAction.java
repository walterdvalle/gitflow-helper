package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public class CheckoutLocalBranchAction extends BaseAction {

    private final Project project;
    private final GitRepository repository;
    private final String branchName;
    private final boolean isCurrent;

    public CheckoutLocalBranchAction(
            Project project,
            GitRepository repository,
            String branchName,
            boolean isCurrent
    ) {
        super(project, branchName, null, null,
                branchName, (isCurrent ? AllIcons.Gutter.Bookmark : AllIcons.Vcs.BranchNode));
        this.project = project;
        this.repository = repository;
        this.branchName = branchName;
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

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                executor.execute(
                        repository.getRoot(),
                        GitCommand.CHECKOUT,
                        branchName
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
