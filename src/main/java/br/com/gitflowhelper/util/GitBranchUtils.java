package br.com.gitflowhelper.util;

import com.intellij.openapi.project.Project;
import git4idea.GitBranch;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

public class GitBranchUtils {

    public static String getCurrentBranchName(Project project) {
        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);

        // Pega o repositório atual (baseado no arquivo selecionado ou raiz do projeto)
        GitRepository repository = manager.getRepositoryForFileQuick(
                project.getBaseDir()
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
