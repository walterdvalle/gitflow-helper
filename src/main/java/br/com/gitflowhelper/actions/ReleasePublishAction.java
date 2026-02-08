package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.git.GitResult;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.GitBranchUtils;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ReleasePublishAction extends BaseAction {

    public ReleasePublishAction(String actionTitle) {
        super(actionTitle, GitFlowDescriptions.RELEASE_PUBLISH.getValue(), AllIcons.CodeWithMe.CwmShared);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProject();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                releasePublish(project);
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release published successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                        getBranchName() != null && getBranchName().startsWith(getReleasePrefix())
        );
    }

    public List<GitResult> releasePublish(Project project) throws GitException {
        List<GitResult> results = new ArrayList<>();
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);

        for (GitRepository repository : repoManager.getRepositories()) {
            VirtualFile root = repository.getRoot();
            String currentBranch = repository.getCurrentBranchName();

            // push release
            results.add(
                    executor.execute(
                            root,
                            GitCommand.PUSH,
                            "-u",
                            REMOTE,
                            currentBranch
                    )
            );

            repository.update();
        }

        return results;
    }
}
