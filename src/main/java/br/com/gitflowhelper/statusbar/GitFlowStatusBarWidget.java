package br.com.gitflowhelper.statusbar;

import br.com.gitflowhelper.popup.GitFlowPopup;
import icons.PluginIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GitFlowStatusBarWidget implements StatusBarWidget {

    private final Project project;
    private StatusBar statusBar;

    private String currentValue = "GitFlowHelper";

    public GitFlowStatusBarWidget(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String ID() {
        return "GitFlowWidget";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return new Presentation();
    }

    private class Presentation implements MultipleTextValuesPresentation {

        @Override
        public @NotNull String getSelectedValue() {
            return currentValue;
        }

        @Override
        public Icon getIcon() {
            return PluginIcons.GitFlow;
        }

        @Override
        public @Nullable String getTooltipText() {
            return "Click to show Git Flow options";
        }

        @Override
        public @Nullable ListPopup getPopup() {
            return new GitFlowPopup(project).getPopup();
        }
    }
}
