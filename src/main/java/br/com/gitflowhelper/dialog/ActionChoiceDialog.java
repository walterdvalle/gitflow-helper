package br.com.gitflowhelper.dialog;

import br.com.gitflowhelper.settings.GitFlowSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ActionChoiceDialog extends DialogWrapper {

    private ComboBox<String> actionComboBox;
    private JBCheckBox keepLocalBranch;
    private JBCheckBox keepRemoteBranch;
    private JBCheckBox squashCommit;
    private JBTextArea commitMessage;
    private String branchName;
    private String targetBranch;
    private String log;
    
    public static final String INTEGRATE = "Integrate immediately";
    public static final String AUTO_CREATE = "Create merge request (Gitlab only)";
    public static final String SELF_CREATE = "I will create a merge/pull request";

    public ActionChoiceDialog(@Nullable Project project, String branchName, String targetBranch) {
        super(project); // true = modal
        this.branchName = branchName;
        this.targetBranch = targetBranch;
        setTitle("Finish feature");
        setOKButtonText("Yes");
        setCancelButtonText("No");
        init();
        setSize(450, 450);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Inicializa a Combo com as opções
        actionComboBox = new ComboBox<>(
                new String[]{
                        INTEGRATE,
                        AUTO_CREATE,
                        SELF_CREATE});
        actionComboBox.setSize(350, 40);
        keepLocalBranch = new JBCheckBox("Keep local branch when finished");
        keepRemoteBranch = new JBCheckBox("Keep remote branch when finished");
        squashCommit = new JBCheckBox("Squash commits (one commit message only)");
        commitMessage = new JBTextArea();
        commitMessage.setRows(30);
        commitMessage.setLineWrap(true);
        commitMessage.setWrapStyleWord(true);
        String defaultMsg = String.format("Merge branch '%s' into %s", branchName, targetBranch);
        commitMessage.setText(defaultMsg);

        squashCommit.addActionListener(e -> {
            if (squashCommit.isSelected()) {
                commitMessage.setText(commitMessage.getText()+"\n\n"+log);
            } else {
                commitMessage.setText(defaultMsg);
            }
        });
        actionComboBox.addActionListener(e -> {
            if (!actionComboBox.getSelectedItem().equals(ActionChoiceDialog.INTEGRATE)) {
                keepRemoteBranch.setSelected(true);
                keepRemoteBranch.setEnabled(false);
            } else {
                keepRemoteBranch.setEnabled(true);
            }
        });

        JBLabel commitLabel = new JBLabel("Commit message:");
        commitLabel.setBorder(JBUI.Borders.emptyBottom(4));

        JBScrollPane commitScroll = new JBScrollPane(commitMessage);
        commitScroll.setPreferredSize(new Dimension(150, 150));

        JBLabel branchLabel = new JBLabel("Finishing feature: "+branchName);
        Font font = branchLabel.getFont();
        Font newFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        branchLabel.setFont(newFont);

        JBLabel explanationLabel = new JBLabel("Select the option above that defines the approval workflow.");
        explanationLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        explanationLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        // Cria o layout usando FormBuilder (padrão do IntelliJ para alinhamento)
        return FormBuilder.createFormBuilder()
                .addComponent(branchLabel)
                .addLabeledComponent("What to do when finished:", actionComboBox)
                .addComponent(explanationLabel)
                .addVerticalGap(10)
                .addComponent(keepLocalBranch)
                .addComponent(keepRemoteBranch)
                .addComponent(squashCommit)
                .addVerticalGap(10)
                .addComponent(commitLabel)
                .addComponent(commitScroll)
                .getPanel();
    }

    public String getSelectedAction() {
        return (String) actionComboBox.getSelectedItem();
    }
    public Boolean getKeepLocalBranch() { return keepLocalBranch.isSelected(); }
    public Boolean getKeepRemoteBranch() { return keepRemoteBranch.isSelected(); }
    public Boolean getSquashCommit() { return squashCommit.isSelected(); }
    public String getCommitMessage() { return commitMessage.getText(); }
    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }
}