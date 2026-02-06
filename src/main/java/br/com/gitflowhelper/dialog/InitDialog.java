package br.com.gitflowhelper.dialog;

import br.com.gitflowhelper.actions.InitAction;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;

public class InitDialog extends DialogWrapper {

    private final GridBagConstraints gbc = new GridBagConstraints();
    private int row = 0;

    private final JTextField mainField    = new JTextField("main");
    private final JTextField developField = new JTextField("develop");
    private final JTextField featureField = new JTextField("feature/");
    private final JTextField releaseField = new JTextField("release/");
    private final JTextField hotfixField  = new JTextField("hotfix/");

    private JPanel panel = new JPanel(new GridBagLayout());

    private Project project;
    private InitAction initAction;

    public InitDialog(Project project, InitAction initAction) {
        super(project);
        this.project = project;
        this.initAction = initAction;
        setTitle("Git Flow Init");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        panel.setPreferredSize(new Dimension(300, 200));

        addRow("Main branch:", mainField);
        addRow("Develop branch:", developField);
        addSeparator();
        addRow("Feature prefix:", featureField);
        addRow("Release prefix:", releaseField);
        addRow("Hotfix prefix:", hotfixField);

        return panel;
    }

    @Override
    public void doOKAction() {
        GitFlowSettingsService settings = GitFlowSettingsService.getInstance(project);
        settings.setMainBranch(mainField.getText());
        settings.setDevelopBranch(developField.getText());
        settings.setFeaturePrefix(featureField.getText());
        settings.setReleasePrefix(releaseField.getText());
        settings.setHotfixPrefix(hotfixField.getText());

        this.initAction.doOKAction(mainField.getText(),  developField.getText(), featureField.getText(), releaseField.getText(), hotfixField.getText());

        super.doOKAction();
    }

    // Padrão: 2 colunas (label + campo)
    public void addRow(String labelText, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        row++;
    }

    // Exceção: separator ocupa as duas colunas
    public void addSeparator() {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 4, 8, 4);

        panel.add(new JSeparator(), gbc);

        // restaura o padrão
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 4, 4, 4);

        row++;
    }
}
