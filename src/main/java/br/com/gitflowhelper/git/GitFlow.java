package br.com.gitflowhelper.git;

import br.com.gitflowhelper.dialog.ActionChoiceDialog;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.ArrayList;
import java.util.List;

public class GitFlow {

    private static final String REMOTE = "origin";

    public static List<GitResult> featureStart(Project project, String featureName) {
        String featureBranch = GitFlowSettingsService.getInstance(project).getFeaturePrefix() + featureName;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);
        List<GitResult> results = new ArrayList<>();

        for (GitRepository repository : repoManager.getRepositories()) {
            VirtualFile root = repository.getRoot();

            // 1. checkout develop
            results.add(
                    executor.execute(root, GitCommand.CHECKOUT, GitFlowSettingsService.getInstance(project).getDevelopBranch())
            );

            // 2. pull develop
            results.add(
                    executor.execute(root, GitCommand.PULL)
            );

            // 3 + 4. create + checkout feature branch
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            "-b",
                            featureBranch,
                            GitFlowSettingsService.getInstance(project).getDevelopBranch()
                    )
            );

            repository.update();
        }
        return results;
    }

    public static List<GitResult> featurePublish(Project project) {
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);

        GitExecutor executor = new GitExecutor(project);
        List<GitResult> results = new ArrayList<>();

        for (GitRepository repository : repoManager.getRepositories()) {
            String currentBranch = repository.getCurrentBranchName();

            if (currentBranch == null) {
                throw new GitException("HEAD is detached");
            }

            results.add(
                    executor.execute(
                            repository.getRoot(),
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

    public static List<GitResult> featureFinish(
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
                }

                // =========================
                // AUTO_CREATE
                // =========================
                case ActionChoiceDialog.AUTO_CREATE -> {

                    // commit changes on feature branch
                    results.add(
                            executor.execute(
                                    root,
                                    GitCommand.COMMIT,
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
                                    "-o", "merge_request.title=" + finalCommitMessage,
                                    "-o", "merge_request.description=" + finalCommitMessage
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
                }
            }

            repository.update();
        }

        return results;
    }
}
