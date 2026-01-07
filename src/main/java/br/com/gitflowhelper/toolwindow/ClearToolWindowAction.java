package br.com.gitflowhelper.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ClearToolWindowAction extends AnAction {

    public ClearToolWindowAction() {
        super("Minha ação", "Executar minha ação", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // lógica do botão
        System.out.println("Botão da ToolWindow clicado");
    }
}