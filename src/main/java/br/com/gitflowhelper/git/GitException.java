package br.com.gitflowhelper.git;

public class GitException extends RuntimeException {

    private final GitResult gitResult;

    public GitException(String message, GitResult gitResult) {
        super(message);
        this.gitResult = gitResult;
    }

    public GitException(String message) {
        super(message);
        gitResult = null;
    }

    public GitResult getGitResult() {
        return gitResult;
    }
}