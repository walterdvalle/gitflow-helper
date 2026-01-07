
package br.com.gitflowhelper.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class GitFlowToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // Instancia nosso painel customizado
        MyToolWindowPanel myToolWindowPanel = new MyToolWindowPanel();

        // Cria o conteúdo usando o ContentFactory do IntelliJ
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(myToolWindowPanel, "", false);

        // Adiciona o conteúdo à ToolWindow
        toolWindow.getContentManager().addContent(content);
    }
}
