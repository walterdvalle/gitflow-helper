package br.com.gitflowhelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import git4idea.GitBranch;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

public class GitBranchUtils {

    public static String getCurrentBranchName(Project project) {
        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);

        GitRepository repository = manager.getRepositoryForFileQuick(
                ProjectUtil.guessProjectDir(project)
        );

        if (repository == null) {
            return null;
        }

        GitBranch branch = repository.getCurrentBranch();

        if (branch != null) {
            return branch.getName();
        }

        // Detached HEAD (ex: checkout em commit)
        return repository.getCurrentRevision();
    }
}
