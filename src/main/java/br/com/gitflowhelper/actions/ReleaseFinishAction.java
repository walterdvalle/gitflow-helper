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
public class ReleaseFinishAction extends BaseAction {

    public ReleaseFinishAction(Project project, String actionTitle, String branchName) {
        super(actionTitle, GitFlowDescriptions.RELEASE_FINISH.getValue(), AllIcons.Vcs.Patch_applied, project, branchName);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
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
                        branchName.startsWith(getReleasePrefix())
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
            String tagMessage = String.format("Merge branch '%s' into %s", releaseBranch, getMainBranch());

            VirtualFile root = repository.getRoot();

            // 1 checkout main
            results.add(
                    executor.execute(root, GitCommand.CHECKOUT, getMainBranch())
            );

            // 2 pull main
            results.add(
                    executor.execute(root, GitCommand.PULL, REMOTE, getMainBranch())
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
                    executor.execute(root, GitCommand.CHECKOUT, getDevelopBranch())
            );

            // 6 pull develop
            results.add(
                    executor.execute(root, GitCommand.PULL, REMOTE, getDevelopBranch())
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
                        executor.execute(root, GitCommand.PUSH, REMOTE, getMainBranch())
                );

                results.add(
                        executor.execute(root, GitCommand.PUSH, REMOTE, getDevelopBranch())
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

            GitResult checkResult = executor.execute(
                    root,
                    GitCommand.LS_REMOTE,
                    "--heads",
                    REMOTE,
                    releaseBranch
            );
            boolean branchExists = checkResult.getExitCode() == 0 &&
                    checkResult.getProcessMessage().contains("refs/heads/" + releaseBranch);

            // 10 delete remote release branch
            if (branchExists && deleteRemoteBranch) {
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
