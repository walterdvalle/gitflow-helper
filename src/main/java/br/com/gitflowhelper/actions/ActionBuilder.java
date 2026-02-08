package br.com.gitflowhelper.actions;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ActionBuilder {

    public static BaseAction createActionInstance(
            String actionClassName,
            String actionTitle) {

        BaseAction actionObj;
        try {
            Class<?> clazz = Class.forName("br.com.gitflowhelper.actions."+actionClassName);

            Constructor<?> ctor = clazz.getConstructor(String.class);

            Object instance = ctor.newInstance(actionTitle);
            actionObj = (BaseAction) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            return null;
        }
        return actionObj;
    }

}
