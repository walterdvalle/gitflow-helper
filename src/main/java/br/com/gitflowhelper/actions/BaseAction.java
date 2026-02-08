package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.statusbar.GitFlowStatusBarWidget;
import br.com.gitflowhelper.util.ActionParamsService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;

public abstract class BaseAction extends AnAction /*implements PropertyChangeListener*/ {
    public static final String REMOTE = "origin";

    /*
    No fields allowed
    Classes based on AnAction must not have class fields of any kind.
    This is because an instance of AnAction class exists for the entire
    lifetime of the application. If the AnAction class uses a field to store
    data that has a shorter lifetime and doesn't clear this data promptly,
    the data leaks. For example, any AnAction data that exists only within
    the context of a Project causes the Project to be kept in memory after
    the user has closed it.

    https://plugins.jetbrains.com/docs/intellij/action-system.html
     */

    public BaseAction(
            String actionTitle,
            String description,
            Icon icon) {
        super(actionTitle, description, icon);
    }

    public String getMainBranch() {
        return GitFlowSettingsService.getInstance(getProject()).getMainBranch();
    }
    public String getDevelopBranch() {
        return GitFlowSettingsService.getInstance(getProject()).getDevelopBranch();
    }
    public String getFeaturePrefix() {
        return GitFlowSettingsService.getInstance(getProject()).getFeaturePrefix();
    }
    public String getReleasePrefix() {
        return GitFlowSettingsService.getInstance(getProject()).getReleasePrefix();
    }
    public String getHotfixPrefix() {
        return GitFlowSettingsService.getInstance(getProject()).getHotfixPrefix();
    }
    public Project getProject() {
        return ActionParamsService.getProject();
    }
    public String getBranchName() {
        return ActionParamsService.getBranchName();
    }

    public void setLoading(boolean loading) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(getProject());
        GitFlowStatusBarWidget sbw = (GitFlowStatusBarWidget) statusBar.getWidget("GitFlowWidget");
        sbw.setLoadding(loading);
        statusBar.updateWidget("GitFlowWidget");
    }
}
