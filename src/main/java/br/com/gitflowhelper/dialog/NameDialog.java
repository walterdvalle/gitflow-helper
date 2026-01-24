package br.com.gitflowhelper.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.function.Consumer;

public class NameDialog extends DialogWrapper {

    private final JTextField nameField = new JTextField();
    private final Consumer<String> onOk;
    private String label;

    public NameDialog(Project project, String titleText, String label, Consumer<String> onOk) {
        super(project);
        this.onOk = onOk;
        setTitle(titleText);
        this.label = label;
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter( new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text != null) {
                    text = text.replace(" ", "_");
                }
                super.insertString(fb, offset, text, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text != null) {
                    text = text.replace(" ", "_");
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(300, 20));
        panel.add(new JLabel(label+":"), BorderLayout.WEST);
        panel.add(nameField, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected void doOKAction() {
        onOk.accept(nameField.getText());
        super.doOKAction();
    }
}
