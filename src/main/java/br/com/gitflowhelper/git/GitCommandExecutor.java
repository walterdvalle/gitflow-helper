package br.com.gitflowhelper.git;

import br.com.gitflowhelper.util.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class GitCommandExecutor {

    private static int lastExitCode = 0;
    private static String lastMessage = "";
    private static String lastErrorMessage = "";

    public static void run(Project project, List<String> command) throws GitException {
        String basePath = project.getBasePath();
        if (basePath == null) return;
        StringBuilder exit = new StringBuilder();

        //GitFlowOutputPanel output = GitFlowOutputPanel.getInstance();

        try {
            PluginUtils.logCommand(project, String.join(" ", command));
            Process process = new ProcessBuilder(command)
                    .directory(new File(basePath))
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    PluginUtils.logOutput(project, line);
                    exit.append(line+"\n");
                }
            }

            lastExitCode = process.waitFor();
            if (isError()) {
                lastErrorMessage = exit.toString();
            } else {
                lastMessage = exit.toString();
            }

            VirtualFileManager.getInstance().asyncRefresh(null);

        } catch (Exception e) {
            PluginUtils.logError(project, "Erro: " + e.getMessage());
        }
        if (isError()) {
            throw new GitException(lastMessage);
        }
    }

    public static boolean isError() {
        return lastExitCode != 0;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
    public static String getLastMessage() {
        return lastMessage;
    }
}
