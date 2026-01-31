package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.ActionChoiceDialog;
import br.com.gitflowhelper.git.*;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitCommit;
import git4idea.commands.GitCommand;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class FeatureFinishAction extends BaseAction {

    private String postAction;
    private String featureCommits = "";
    private static final String ACTION_DESCRIPTION = "Merges current branch into develop chain and deletes current branch or creates a merge request (git flow feature finish).";

    public FeatureFinishAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Vcs.Patch_applied, ACTION_DESCRIPTION);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ActionChoiceDialog dialog = new ActionChoiceDialog(project, branchName, getDevelopBranch());

        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        try {
            for (GitRepository repository : repoManager.getRepositories()) {
                VirtualFile root = repository.getRoot();
                //grabs from the first
                if (featureCommits.equals("")) {
                    featureCommits = getFeatureCommits(project, repository, getDevelopBranch(), branchName);
                    break;
                }
            }
            dialog.setLog(featureCommits);
        } catch (VcsException ex) {
            NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getMessage());
        }

        if (dialog.showAndGet()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    setLoading(true);
                    try {
                        featureFinish(
                            project,
                            branchName,
                            dialog.getSquashCommit(),
                            dialog.getCommitMessage(),
                            !dialog.getKeepLocalBranch(),
                            !dialog.getKeepRemoteBranch(),
                            dialog.getSelectedAction(),
                            true);
                        NotificationUtil.showGitFlowSuccessNotification(project, "Success",  postAction);
                    } catch (GitException ex) {
                        NotificationUtil.showGitFlowErrorNotification(project, "Error", "Error message: "+ex.getGitResult().getProcessMessage());
                    }
                    setLoading(false);
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                branchName.startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())
        );
    }

    private String getFeatureCommits(
            Project project, GitRepository repository,
            String baseBranch, String featureBranch) throws VcsException {
        List<GitCommit> commits = GitHistoryUtils.history(
                project,
                repository.getRoot(),
                baseBranch + ".." + featureBranch
        );
        return commits.stream()
                .map(c -> "- " + c.getFullMessage())
                .collect(Collectors.joining("\n"));

    }

    private List<GitResult> featureFinish(
            Project project,
            String featureBranch,
            boolean squash,
            String finalCommitMessage,
            boolean deleteLocalBranch,
            boolean deleteRemoteBranch,
            String mode,
            boolean rebaseBeforeIntegrate) {
        String baseBranch = GitFlowSettingsService.getInstance(project).getDevelopBranch();

        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);
        List<GitResult> results = new ArrayList<>();

        for (GitRepository repository : repoManager.getRepositories()) {
            VirtualFile root = repository.getRoot();

            switch (mode) {

                // =========================
                // INTEGRATE
                // =========================
                case ActionChoiceDialog.INTEGRATE -> {

                    // checkout develop
                    results.add(
                            executor.execute(root, GitCommand.CHECKOUT, baseBranch)
                    );

                    if (rebaseBeforeIntegrate) {
                        // fetch
                        executor.execute(root, GitCommand.FETCH);

                        // rebase develop
                        executor.execute(root, GitCommand.REBASE, baseBranch);
                    }

                    // pull develop
                    results.add(
                            executor.execute(root, GitCommand.PULL)
                    );

                    // merge
                    if (squash) {
                        results.add(
                                executor.execute(
                                        root,
                                        GitCommand.MERGE,
                                        "--squash",
                                        featureBranch
                                )
                        );

                        results.add(
                                executor.execute(
                                        root,
                                        GitCommand.COMMIT,
                                        "-m",
                                        finalCommitMessage
                                )
                        );
                    } else {
                        results.add(
                                executor.execute(
                                        root,
                                        GitCommand.MERGE,
                                        "--no-ff",
                                        "-m",
                                        finalCommitMessage,
                                        featureBranch
                                )
                        );
                    }

                    // push to develop
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.PUSH,
                                    REMOTE,
                                    baseBranch
                            )
                    );

                    postAction = "Feature finished and pushed to " + getDevelopBranch() + " successfully.";

                }

                // =========================
                // SELF_CREATE
                // =========================
                case ActionChoiceDialog.SELF_CREATE -> {

                    // commit changes on feature branch
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.COMMIT,
                                    "--allow-empty",
                                    "-m",
                                    finalCommitMessage
                            )
                    );

                    // push feature branch
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.PUSH,
                                    REMOTE,
                                    featureBranch
                            )
                    );

                    // checkout develop
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.CHECKOUT,
                                    baseBranch
                            )
                    );

                    postAction = "Feature pushed to " + branchName + " successfully. Create yourself a merge/pull request.";
                }

                // =========================
                // AUTO_CREATE
                // =========================
                case ActionChoiceDialog.AUTO_CREATE -> {

                    String title = finalCommitMessage.substring(0, finalCommitMessage.indexOf("\n"));
                    StringBuilder description = new StringBuilder();
                    for (String line : finalCommitMessage.substring(finalCommitMessage.indexOf("\n")+1).split("\\R")) {
                        description.append(line).append("<br/>");
                    }
                    // commit changes on feature branch
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.COMMIT,
                                    "--allow-empty",
                                    "-m",
                                    finalCommitMessage
                            )
                    );

                    // push with GitLab MR options
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.PUSH,
                                    REMOTE,
                                    featureBranch,
                                    "-o", "merge_request.create",
                                    "-o", "merge_request.target=" + baseBranch,
                                    "-o", "merge_request.title=" + title,
                                    "-o", "merge_request.description=" + description
                            )
                    );

                    // checkout develop
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.CHECKOUT,
                                    baseBranch
                            )
                    );

                    postAction = "Feature pushed to " + branchName + " and merge request created successfully.";
                }
            }
            // delete local branch
            if (deleteLocalBranch) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.BRANCH,
                                "-d",
                                featureBranch
                        )
                );
            }

            // delete remote branch
            if (deleteRemoteBranch) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.PUSH,
                                REMOTE,
                                "--delete",
                                featureBranch
                        )
                );
            }


            repository.update();
        }

        return results;
    }
}
