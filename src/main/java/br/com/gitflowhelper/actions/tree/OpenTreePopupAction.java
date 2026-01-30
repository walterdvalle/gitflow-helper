package br.com.gitflowhelper.actions.tree;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

public class OpenTreePopupAction extends AnAction {

    public OpenTreePopupAction(String actionTitle)  {
        super(actionTitle);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project == null) return;

        SampleTreePanel panel = new SampleTreePanel(project);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, panel)
                .setTitle("My Tree")
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true)
                .setMinSize(new java.awt.Dimension(300, 400))
                .createPopup();

        popup.showInBestPositionFor(e.getDataContext());
    }
}
