package br.com.gitflowhelper.git;

import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public final class GitBranchDetector {

    private GitBranchDetector() {
        // utilitário estático
    }

    public static String currentBranch(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }

        try {
            Process process = new ProcessBuilder("git", "branch", "--show-current")
                    .directory(new File(basePath))
                    .start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
