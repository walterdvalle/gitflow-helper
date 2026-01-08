package br.com.gitflowhelper.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ActionChoiceDialog extends DialogWrapper {

    private ComboBox<String> actionComboBox;
    private JBCheckBox keepBranch;

    public ActionChoiceDialog(@Nullable Project project) {
        super(project); // true = modal por padrão
        setTitle("Are you sure?");
        
        // Configura os botões para "Yes" e "No" conforme solicitado
        setOKButtonText("Yes");
        setCancelButtonText("No");

        init(); // Inicializa o diálogo (obrigatório)
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Inicializa a Combo com as opções
        actionComboBox = new ComboBox<>(new String[]{"Push immediately", "Create merge request"});
        keepBranch = new JBCheckBox("Keep branch when finished");

        JBLabel explanationLabel = new JBLabel("Select the option above that defines the approval workflow.");
        explanationLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        explanationLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        // Cria o layout usando FormBuilder (padrão do IntelliJ para alinhamento)
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("What to do when finished:", actionComboBox)
                .addComponent(explanationLabel) // Adiciona o label na linha de baixo
                .addComponent(keepBranch)
                .addVerticalGap(10)
                .getPanel();
    }

    public String getSelectedAction() {
        return (String) actionComboBox.getSelectedItem();
    }
    public Boolean getKeepBranch() {
        return keepBranch.isEnabled();
    }
}