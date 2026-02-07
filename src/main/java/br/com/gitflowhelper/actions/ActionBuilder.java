package br.com.gitflowhelper.actions;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ActionBuilder {

    public static BaseAction createActionInstance(
            String actionClassName,
            Project project,
            String actionTitle,
            String branchName) {

        BaseAction actionObj;
        try {
            Class<?> clazz = Class.forName("br.com.gitflowhelper.actions."+actionClassName);

            Constructor<?> ctor = clazz.getConstructor(
                    Project.class,
                    String.class,
                    String.class
            );

            Object instance = ctor.newInstance(
                    project, actionTitle, branchName
            );
            actionObj = (BaseAction) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            return null;
        }
        return actionObj;
    }

}
