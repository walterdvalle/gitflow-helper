package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.statusbar.GitFlowStatusBarWidget;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class BaseAction extends AnAction implements PropertyChangeListener {
    public static final String REMOTE = "origin";

    protected Project project;
    protected String type;
    protected String action;
    protected String branchName;
    protected String actionDescription;

    public BaseAction(Project project, String actionTitle, String type, String action, String branchName, Icon icon, String description) {
        super(actionTitle, description, icon);
        this.project = project;
        this.type = type;
        this.action = action;
        this.branchName = branchName;
        this.actionDescription = description;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.branchName = (String) evt.getNewValue();
    }

    public String getMainBranch() {
        return GitFlowSettingsService.getInstance(project).getMainBranch();
    }
    public String getDevelopBranch() {
        return GitFlowSettingsService.getInstance(project).getDevelopBranch();
    }

    public void setLoading(boolean loading) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        GitFlowStatusBarWidget sbw = (GitFlowStatusBarWidget) statusBar.getWidget("GitFlowWidget");
        sbw.setLoadding(loading);
        statusBar.updateWidget("GitFlowWidget");
    }
}
