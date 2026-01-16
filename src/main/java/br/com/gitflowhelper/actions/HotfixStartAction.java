package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("unused")
public class HotfixStartAction extends BaseAction {

    public HotfixStartAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new NameDialog(project, type + " start", "Hotfix description", name ->
        {
            try {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "_")).split(" "))
                );
            } catch (Exception ex) {
                NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                return;
            }
            NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New hotfix created successfully");
        }
        ).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(GitFlowSettingsService.getInstance(project).getMainBranch()) &&
                branchName.equals(GitFlowSettingsService.getInstance(project).getMainBranch())
        );
    }
}
