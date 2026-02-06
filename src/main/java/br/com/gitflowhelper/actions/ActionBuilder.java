package br.com.gitflowhelper.actions;

import com.intellij.openapi.project.Project;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ActionBuilder {

    public static BaseAction createActionInstance(String actionClassName, String type, String action, String actionTitle,
                                                  Project project, String branchName)  {
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
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            return null;
        }
        return actionObj;
    }

}
