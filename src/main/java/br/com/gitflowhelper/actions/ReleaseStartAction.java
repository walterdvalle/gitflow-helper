package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.git.GitResult;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
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
public class ReleaseStartAction extends BaseAction {

    public ReleaseStartAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new NameDialog(project, type + " start", "Version description", true, (name) ->
        {
//            try {
//                GitCommandExecutor.run(
//                        project,
//                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
//                );
//            } catch (GitException ex) {
//                NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
//                return;
//            }
//            NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release created successfully");
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                setLoading(true);
                try {
                    releaseStart(name.getName(), name.getPushOnFinish());
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release created successfully");
                } catch (GitException ex) {
                    NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
                }
                setLoading(false);
            });
        }
        ).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                branchName.equals(getDevelopBranch())
        );
    }

    public List<GitResult> releaseStart(String releaseName, boolean push) {
        String releaseBranch = GitFlowSettingsService.getInstance(project).getReleasePrefix() + releaseName;
        List<GitResult> results = new ArrayList<>();
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);

        for (GitRepository repository : repoManager.getRepositories()) {

            VirtualFile root = repository.getRoot();

            // 1 checkout develop
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            GitFlowSettingsService.getInstance(project).getDevelopBranch()
                    )
            );

            // 2 pull develop
            results.add(
                    executor.execute(
                            root,
                            GitCommand.PULL,
                            REMOTE,
                            GitFlowSettingsService.getInstance(project).getDevelopBranch()
                    )
            );

            // 3 create and checkout release branch
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            "-b",
                            releaseBranch
                    )
            );

            // 4 push release (opcional)
            if (push) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.PUSH,
                                "-u",
                                REMOTE,
                                releaseBranch
                        )
                );
            }

            // 5 sync with IntelliJ
            repository.update();
        }

        return results;
    }
}
