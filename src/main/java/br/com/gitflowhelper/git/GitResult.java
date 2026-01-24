package br.com.gitflowhelper.git;

public class GitResult {

    private final int exitCode;
    private final String command;
    private final String processMessage;

    public GitResult(int exitCode, String command, String processMessage) {
        this.exitCode = exitCode;
        this.command = command;
        this.processMessage = processMessage;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getCommand() {
        return command;
    }

    public String getProcessMessage() {
        return processMessage;
    }
}
