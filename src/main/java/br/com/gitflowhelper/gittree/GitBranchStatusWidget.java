package br.com.gitflowhelper.gittree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;

public class GitBranchStatusWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    private final Project project;
    private StatusBar statusBar;

    public GitBranchStatusWidget(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String ID() {
        return "GitBranchStatusWidget";
    }

    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {}

    // Status bar text
    @Override
    public @NotNull String getText() {
        return "Git Branches";
    }

    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    @Override
    public @Nullable String getTooltipText() {
        return "Show Git branches";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return event -> {
            JBPopup popup = GitBranchPopupBuilder.createPopup(project);
            popup.showUnderneathOf(event.getComponent());
        };
    }
}
