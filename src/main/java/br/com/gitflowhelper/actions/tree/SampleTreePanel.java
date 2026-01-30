package br.com.gitflowhelper.actions.tree;

import com.intellij.openapi.project.Project;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import java.awt.*;

public class SampleTreePanel extends JPanel {

    public SampleTreePanel(Project project) {
        super(new BorderLayout());

        SampleTreeStructure structure = new SampleTreeStructure(project);

        StructureTreeModel structureModel =
                new StructureTreeModel(structure, null, project);

        AsyncTreeModel asyncModel =
                new AsyncTreeModel(structureModel, project);

        Tree tree = new Tree(asyncModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        new TreeSpeedSearch(tree);

        add(new JScrollPane(tree), BorderLayout.CENTER);
    }
}
