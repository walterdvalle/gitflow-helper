package br.com.gitflowhelper.git;

import br.com.gitflowhelper.util.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class GitCommandExecutor {

    public static void run(Project project, List<String> command) {
        String basePath = project.getBasePath();
        if (basePath == null) return;

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
                }
            }

            process.waitFor();

            VirtualFileManager.getInstance().asyncRefresh(null);

        } catch (Exception e) {
            PluginUtils.logError(project, "Erro: " + e.getMessage());
        }
    }
}
