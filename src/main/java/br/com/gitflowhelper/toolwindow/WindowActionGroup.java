package br.com.gitflowhelper.toolwindow;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

import javax.swing.*;

public class WindowActionGroup extends DefaultActionGroup {

    public WindowActionGroup(JTextPane textPane) {
        add(new ClearToolWindowAction(textPane));
        addSeparator();
    }
}
