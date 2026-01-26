package br.com.gitflowhelper.actions;

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
public class ReleaseFinishAction extends BaseAction {

    public ReleaseFinishAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Vcs.Patch_applied);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        try {
//            GitCommandExecutor.run(
//                    project,
//                    Arrays.asList(String.format("git rebase origin/%s", getDevelopBranch()).split(" "))
//            );
//            GitCommandExecutor.run(
//                    project,
//                    Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
//            );
//            GitCommandExecutor.run(project, Arrays.asList("git push".split(" ")));
//            GitCommandExecutor.run(project, Arrays.asList("git push --tags".split(" ")));
//        } catch (GitException ex) {
//            NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
//            return;
//        }
//
//        NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Released finished and tag pushed successfully");

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                releaseFinish(true, true, true);
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Released finished and tag pushed successfully");
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
                branchName.startsWith(GitFlowSettingsService.getInstance(project).getReleasePrefix())
        );
    }

    public List<GitResult> releaseFinish(
            boolean deleteLocalBranch,
            boolean deleteRemoteBranch,
            boolean tagAndPush
    ) {
        List<GitResult> results = new ArrayList<>();
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);

        for (GitRepository repository : repoManager.getRepositories()) {
            String releaseName = repository.getCurrentBranchName().substring(
                    repository.getCurrentBranchName().indexOf("/")+1
            );
            String releaseBranch = repository.getCurrentBranchName();
            String tagMessage = String.format("Merge branch '%s' into %s", releaseBranch, GitFlowSettingsService.getInstance(project).getMainBranch());

            VirtualFile root = repository.getRoot();

            // 1 checkout main
            results.add(
                    executor.execute(root, GitCommand.CHECKOUT, GitFlowSettingsService.getInstance(project).getMainBranch())
            );

            // 2 pull main
            results.add(
                    executor.execute(root, GitCommand.PULL, REMOTE, GitFlowSettingsService.getInstance(project).getMainBranch())
            );

            // 3 merge release -> main
            results.add(
                    executor.execute(
                            root,
                            GitCommand.MERGE,
                            "--no-ff",
                            "-m",
                            tagMessage,
                            releaseBranch
                    )
            );

            // 4 cria tag
            if (tagAndPush) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.TAG,
                                "-a",
                                releaseName,
                                "-m",
                                tagMessage
                        )
                );
            }

            // 5 checkout develop
            results.add(
                    executor.execute(root, GitCommand.CHECKOUT, GitFlowSettingsService.getInstance(project).getDevelopBranch())
            );

            // 6 pull develop
            results.add(
                    executor.execute(root, GitCommand.PULL, REMOTE, GitFlowSettingsService.getInstance(project).getDevelopBranch())
            );

            // 7 merge release -> develop
            results.add(
                    executor.execute(
                            root,
                            GitCommand.MERGE,
                            "--no-ff",
                            "-m",
                            tagMessage,
                            releaseBranch
                    )
            );

            // 8 push branches + tag
            if (tagAndPush) {
                results.add(
                        executor.execute(root, GitCommand.PUSH, REMOTE, GitFlowSettingsService.getInstance(project).getMainBranch())
                );

                results.add(
                        executor.execute(root, GitCommand.PUSH, REMOTE, GitFlowSettingsService.getInstance(project).getDevelopBranch())
                );

                results.add(
                        executor.execute(root, GitCommand.PUSH, REMOTE, releaseName)
                );
            }

            // 9 delete local release branch
            if (deleteLocalBranch) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.BRANCH,
                                "-d",
                                releaseBranch
                        )
                );
            }

            // 10 delete remote release branch
            if (deleteRemoteBranch) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.PUSH,
                                REMOTE,
                                "--delete",
                                releaseBranch
                        )
                );
            }

            repository.update();
        }

        return results;
    }
}
