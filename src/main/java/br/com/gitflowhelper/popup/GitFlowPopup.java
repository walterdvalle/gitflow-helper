package br.com.gitflowhelper.popup;

import br.com.gitflowhelper.actions.ActionBuilder;
import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.actions.InitAction;
import br.com.gitflowhelper.util.GitBranchUtils;
import br.com.gitflowhelper.util.PropertyObserver;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public final class GitFlowPopup extends PropertyObserver {
    private ListPopup listPopup;
    private final Project project;
    private String branchName;

    public GitFlowPopup(Project project) {
        this.branchName = "";
        this.project = project;

        DataManager.getInstance()
            .getDataContextFromFocusAsync()
            .onSuccess(dataContext -> {

                this.listPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                        "Git Flow",
                        createGroup(),
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true
                );
                //slow job
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    this.branchName = GitBranchUtils.getCurrentBranchName(project);
                    firePropertyChange("branchName", "", this.branchName);
                });

                this.listPopup.addListener(new JBPopupListener() {
                       @Override
                       public void beforeShown(@NotNull LightweightWindowEvent event) {
                           var oldPlace = listPopup.getLocationOnScreen();
                           var newPlace = new Point((int) oldPlace.getX(), (int) oldPlace.getY()+20);
                           listPopup.setLocation(newPlace);
                           JBPopupListener.super.beforeShown(event);
                       }
                   }
                );
            });
    }

    private DefaultActionGroup createGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new InitAction(project, "Init..."));
        group.addSeparator();
        group.add(flowGroup("Feature", AllIcons.Vcs.Branch));
        group.add(flowGroup("Release", AllIcons.Ide.Gift));
        group.add(flowGroup("Hotfix", AllIcons.General.ExternalTools));
        return group;
    }

    public ListPopup getPopup() {
        return this.listPopup;
    }

    private DefaultActionGroup flowGroup(String type, Icon icon) {
        DefaultActionGroup group = new DefaultActionGroup(type, type, icon);
        group.setPopup(true);
        group.add(flowAction(type, "start"));
        group.add(flowAction(type, "publish"));
        group.add(flowAction(type, "finish"));
        return group;
    }

    private AnAction flowAction(String type, String action) {
        String actionTitle = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);
        BaseAction act = ActionBuilder.createActionInstance(
                type+actionTitle+"Action", type, action, actionTitle, project, branchName);
        addPropertyChangeListener(act);
        return act;
    }

}
