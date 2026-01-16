package br.com.gitflowhelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class BaseAction extends AnAction implements PropertyChangeListener {
    protected Project project;
    protected String type;
    protected String action;
    protected String branchName;

    public BaseAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(actionTitle);
        this.project = project;
        this.type = type;
        this.action = action;
        this.branchName = branchName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.branchName = (String) evt.getNewValue();
    }
}
