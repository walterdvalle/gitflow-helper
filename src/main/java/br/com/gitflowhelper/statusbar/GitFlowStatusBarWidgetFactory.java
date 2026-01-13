package br.com.gitflowhelper.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

public class GitFlowStatusBarWidgetFactory
        implements StatusBarWidgetFactory {

    @Override
    public @NotNull String getId() {
        return "GitFlowWidget";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Git Flow";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new GitFlowStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {}
}
