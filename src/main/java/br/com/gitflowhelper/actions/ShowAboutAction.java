package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.AboutDialog;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowAboutAction extends BaseAction {

    public ShowAboutAction(String actionTitle) {
        super(actionTitle, GitFlowDescriptions.ABOUT.getValue(), AllIcons.General.Information);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        AboutDialog dialog = new AboutDialog(project);
        dialog.show();
    }
}