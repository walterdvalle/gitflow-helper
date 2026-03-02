package br.com.gitflowhelper.gittree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

public class GitBranchStatusBarFactory implements StatusBarWidgetFactory {

    @NotNull
    @Override
    public String getId() {
        return "GitBranchStatusWidget";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Git Branch Status Widget";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new GitBranchStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
    }
}