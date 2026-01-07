package br.com.gitflowhelper.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;

public class GitToolWindowIntegration {

    public static GitCommandResult run(Project project,
                                       GitCommand gitCommand,
                                       String... parameters) {

        GitRepository repository = GitUtil.getRepositoryManager(project)
                .getRepositories()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Nenhum repositório Git encontrado"));

        VirtualFile root = repository.getRoot();

        GitLineHandler handler =
                new GitLineHandler(project, root, gitCommand);

        handler.addParameters(parameters);

        handler.setStdoutSuppressed(false);
        handler.setStderrSuppressed(false);

        return Git.getInstance().runCommand(handler);
    }
}

