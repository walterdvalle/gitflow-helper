package br.com.gitflowhelper.util;

import br.com.gitflowhelper.toolwindow.ToolWindowPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import javax.swing.SwingUtilities;

public class PluginUtils {

    public static void logError(Project project, String message) {
        logToMyWindow(project, "<pre style=\"margin:0; padding:0;\" style=\"color: red;\">"+message+"</pre>");
    }

    public static void logCommand(Project project, String message) {
        logToMyWindow(project, "<pre style=\"margin:0; padding:0;\" style=\"color: blue;\">$ "+message+"</pre>");
    }

    public static void logOutput(Project project, String message) {
        logToMyWindow(project, "<pre style=\"margin:0; padding:0;\">"+message+"</pre>");
    }

    private static void logToMyWindow(Project project, String message) {
        SwingUtilities.invokeLater(() -> {
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = toolWindowManager.getToolWindow("GitFlowOutput");

            if (toolWindow != null && toolWindow.getContentManager().getContentCount() > 0) {
                Content content = toolWindow.getContentManager().getContent(0);

                // O cast continua o mesmo, pois o nome da classe do painel não mudou
                if (content != null && content.getComponent() instanceof ToolWindowPanel) {
                    ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
                    panel.append(message);
                }
            }
        });
    }
}