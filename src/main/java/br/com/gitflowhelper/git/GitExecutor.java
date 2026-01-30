package br.com.gitflowhelper.git;

import br.com.gitflowhelper.util.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;

public class GitExecutor {

    private final Project project;
    private final Git git;

    public GitExecutor(Project project) {
        this.project = project;
        this.git = Git.getInstance();
    }

    public GitResult execute(VirtualFile root, GitCommand command, String... parameters) {
        GitLineHandler handler = new GitLineHandler(project, root, command);
        handler.addParameters(parameters);

        GitCommandResult result = git.runCommand(handler);

        PluginUtils.logCommand(project, handler.printableCommandLine());

        String output = String.join("\n", result.getOutput());
        String error = String.join("\n", result.getErrorOutput());
        String message = output + (error.isEmpty() ? "" : "\n" + error);

        int exitCode = result.success() ? 0 : 1;

        GitResult gitResult = new GitResult(exitCode, handler.printableCommandLine(), message);

        if (exitCode == 0) {
            PluginUtils.logOutput(project, message);
        } else {
            PluginUtils.logError(project, message);
            throw new GitException(gitResult.getProcessMessage(), gitResult);
        }

        return gitResult;
    }
}
