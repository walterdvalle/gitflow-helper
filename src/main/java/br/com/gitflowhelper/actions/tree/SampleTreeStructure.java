package br.com.gitflowhelper.actions.tree;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SampleTreeStructure extends AbstractTreeStructure {

    private final Project project;
    private final Object rootElement = "Root";

    public SampleTreeStructure(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull Object getRootElement() {
        return rootElement;
    }

    @Override
    public Object @NotNull [] getChildElements(@NotNull Object element) {
        if ("Root".equals(element)) {
            return new Object[]{"Item 1", "Item 2"};
        }
        return SimpleNode.EMPTY_ARRAY;
    }

    // 🔴 ESTA é a assinatura que o seu SDK exige
    @Override
    public @NotNull NodeDescriptor createDescriptor(
            @NotNull Object element,
            @Nullable NodeDescriptor parentDescriptor
    ) {
        return new SampleTreeNode(project, parentDescriptor, element.toString());
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        return null;
    }

    @Override
    public void commit() {
    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(@NotNull Object element) {
        return !"Root".equals(element);
    }
}
