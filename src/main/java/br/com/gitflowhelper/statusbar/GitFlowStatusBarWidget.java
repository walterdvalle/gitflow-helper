package br.com.gitflowhelper.statusbar;

import br.com.gitflowhelper.popup.GitFlowPopup;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.rt.coverage.util.CoverageIOUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class GitFlowStatusBarWidget
        implements StatusBarWidget, StatusBarWidget.IconPresentation {

    private final Project project;

    public GitFlowStatusBarWidget(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String ID() {
        return "GitFlowWidget";
    }

    @Override
    public Icon getIcon() {
        return com.intellij.icons.ExpUiIcons.General.Vcs;
    }

    @Override
    public String getTooltipText() {
        return "Git Flow Helper";
    }

    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return e -> GitFlowPopup.show(project, e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void dispose() {}
}
