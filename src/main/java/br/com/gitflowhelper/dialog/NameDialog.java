package br.com.gitflowhelper.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.function.Consumer;

public class NameDialog extends DialogWrapper {

    private final JTextField nameField = new JTextField();
    private JBCheckBox pushOnFinish;
    private final Consumer<NameResponse> onOk;
    private String label;
    private boolean showPush;

    public NameDialog(Project project, String titleText, String label, boolean showPush, Consumer<NameResponse> onOk) {
        super(project);
        this.onOk = onOk;
        setTitle(titleText);
        this.label = label;
        this.showPush = showPush;
        this.pushOnFinish = new JBCheckBox("Push local branch when finished");
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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label + ":"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(Box.createHorizontalStrut(0), gbc);

        if (showPush) {
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            panel.add(pushOnFinish, gbc);
        }

        return panel;
    }

    @Override
    protected void doOKAction() {
        onOk.accept(new NameResponse(nameField.getText(), pushOnFinish.isSelected()));
        super.doOKAction();
    }
}
