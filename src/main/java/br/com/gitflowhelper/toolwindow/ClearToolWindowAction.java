package br.com.gitflowhelper.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ClearToolWindowAction extends AnAction {

    private JTextPane textPane;

    public ClearToolWindowAction(JTextPane textPane) {
        super(
            "Clear",
            "Clear the text area",
            AllIcons.Actions.GC
        );
        this.textPane = textPane;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        int result = Messages.showYesNoDialog(
            e.getProject(),
            "Clear all git flow logs?",
            "Confirmation",
            Messages.getQuestionIcon()
        );
        if (result == Messages.YES) {
            this.textPane.setText("<html><body></body></html>");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Exemplo: habilitar/desabilitar dinamicamente
        boolean hasData = true; // substitua por lógica real
        e.getPresentation().setEnabled(hasData);
    }
}
