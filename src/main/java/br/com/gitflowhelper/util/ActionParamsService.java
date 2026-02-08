package br.com.gitflowhelper.util;

import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.project.Project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class ActionParamsService implements PropertyChangeListener {

    private static final ActionParamsService INSTANCE = new ActionParamsService();
    private static Project project;
    private static String branchName;

    private ActionParamsService() { }

    public static ActionParamsService getInstance() {
        return INSTANCE;
    }

    public static void setProject(Project project) {
        ActionParamsService.project = project;
    }
    public static void setBranchName(String branchName) {
        ActionParamsService.branchName = branchName;
    }
    public static Project getProject() {
        return project;
    }
    public static String getBranchName() {
        return branchName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        ActionParamsService.setBranchName((String) e.getNewValue());
        ActivityTracker.getInstance().inc();
    }
}
