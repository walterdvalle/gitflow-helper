package br.com.gitflowhelper.gittree;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

import javax.swing.*;

public class BranchNode {
    private final String name;
    private final Icon icon;
    private final boolean current;
    private final boolean remote;
    private final DefaultActionGroup group;

    public BranchNode(String name, Icon icon, boolean current, boolean remote, DefaultActionGroup group) {
        this.name = name;
        this.icon = icon;
        this.current = current;
        this.remote = remote;
        this.group = group;
    }

    public String getName() { return name; }
    public Icon getIcon() { return icon; }
    public boolean isCurrent() { return current; }
    public boolean isRemote() { return remote; }
    public DefaultActionGroup getGroup() { return group;}

    @Override
    public String toString() {
        return name;
    }
}
