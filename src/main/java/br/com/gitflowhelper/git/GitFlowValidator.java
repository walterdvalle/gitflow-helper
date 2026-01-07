package br.com.gitflowhelper.git;

import com.intellij.openapi.project.Project;

import java.io.File;

public final class GitFlowValidator {

    private GitFlowValidator() {
        // utilitário estático
    }

    public static boolean isGitFlowInstalled(Project project) {
        try {
            String basePath = project.getBasePath();
            if (basePath == null) {
                return false;
            }

            Process process = new ProcessBuilder("git", "flow", "version")
                    .directory(new File(basePath))
                    .start();

            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
