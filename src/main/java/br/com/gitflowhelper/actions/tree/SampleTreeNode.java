package br.com.gitflowhelper.actions.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SampleTreeNode extends NodeDescriptor {

    private final String element;

    public SampleTreeNode(
            @NotNull Project project,
            @Nullable NodeDescriptor parent,
            @NotNull String element
    ) {
        super(project, parent);
        this.element = element;

        myName = element;
        setIcon(AllIcons.Nodes.Folder);
    }

    // 🔴 OBRIGATÓRIO no seu SDK
    @Override
    public @NotNull Object getElement() {
        return element;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public String toString() {
        return element;
    }
}
