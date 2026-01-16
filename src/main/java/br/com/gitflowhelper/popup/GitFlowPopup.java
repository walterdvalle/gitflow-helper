package br.com.gitflowhelper.popup;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.actions.InitAction;
import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.util.GitBranchUtils;
import br.com.gitflowhelper.util.NotificationUtil;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.dialog.InitDialog;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.dialog.ActionChoiceDialog;
import br.com.gitflowhelper.util.PropertyObserver;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
                           var newPlace = new Point((int) oldPlace.getX(), (int) oldPlace.getY()+40);
                           listPopup.setLocation(newPlace);
                           JBPopupListener.super.beforeShown(event);
                       }
                   }
                );
            });
    }

    private DefaultActionGroup createGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new InitAction(project, "Init"));
        group.addSeparator();
        group.add(flowGroup("Feature"));
        group.add(flowGroup("Release"));
        group.add(flowGroup("Hotfix" ));
        return group;
    }

    public ListPopup getPopup() {
        return this.listPopup;
    }

    private DefaultActionGroup flowGroup(String type) {
        DefaultActionGroup group = new DefaultActionGroup(type, true);
        group.add(flowAction(type, "start"));
        group.add(flowAction(type, "publish"));
        group.add(flowAction(type, "finish"));
        return group;
    }

    private AnAction flowAction(String type, String action) {
        String actionTitle = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);
        BaseAction act = createActionInstance(type+actionTitle+"Action", type, action, actionTitle);
        addPropertyChangeListener(act);
        return act;
    }

    private BaseAction createActionInstance(String actionClassName, String type, String action, String actionTitle)  {
        BaseAction actionObj;
        try {
            Class<?> clazz = Class.forName("br.com.gitflowhelper.actions."+actionClassName);

            Constructor<?> ctor = clazz.getConstructor(
                    Project.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class
            );

            Object instance = ctor.newInstance(
                    project, actionTitle, type, action, branchName
            );
            actionObj = (BaseAction) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return actionObj;
    }
}
