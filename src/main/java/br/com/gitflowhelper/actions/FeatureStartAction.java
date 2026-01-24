package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitFlow;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("unused")
public class FeatureStartAction extends BaseAction {

    public FeatureStartAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Actions.Execute);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        new NameDialog(project, type + " start", "Feature description", name ->
        {
            try {
//                GitCommandExecutor.run(
//                        project,
//                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
//                );
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    GitFlow.featureStart(project, name);
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature created successfully");
                });
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
//                return;
            }
//            NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature created successfully");
        }
        ).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                branchName.equals(getDevelopBranch())
        );
    }
}
