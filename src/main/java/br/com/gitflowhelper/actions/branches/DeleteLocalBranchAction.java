package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.util.ActionParamsService;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.repo.GitRepository;

public class DeleteLocalBranchAction extends BaseAction {

    public DeleteLocalBranchAction(
            String label,
            String localBranchName) {
        super(label,
                GitFlowDescriptions.DELETE_LOCAL.getValue() + localBranchName,
                AllIcons.Vcs.Remove);
    }

    @Override
    public void update(AnActionEvent e) {
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
    public void actionPerformed(AnActionEvent e) {
        Project project = getProject();
        GitRepository repository = ActionParamsService.getRepo(this);
        String currentBranchName = repository.getCurrentBranchName();
        String localBranchName = ActionParamsService.getName(this);
        boolean isCurrent = currentBranchName.equals(localBranchName);

        if (isCurrent) return;

        int confirm = Messages.showYesNoDialog(
                project,
                "Delete local branch '" + localBranchName + "'?",
                "Delete Branch",
                Messages.getQuestionIcon()
        );

        if (confirm != Messages.YES) return;


        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                delete(repository, project, localBranchName);
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Local branch "+localBranchName+" deleted successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });

    }

    private void delete(GitRepository repository, Project project, String localBranchName) {
        GitExecutor executor = new GitExecutor(project);
        executor.execute(
                repository.getRoot(),
                git4idea.commands.GitCommand.BRANCH,
                "-d",
                localBranchName
        );
        repository.update();
    }

}
