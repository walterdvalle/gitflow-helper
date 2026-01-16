package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.ActionChoiceDialog;
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
public class FeatureFinishAction extends BaseAction {

    public FeatureFinishAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ActionChoiceDialog dialog = new ActionChoiceDialog(project);
        String acaoPos;
        if (dialog.showAndGet()) {
            String keep = dialog.getKeepBranch() ? " --keep" : "";
            try {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(("git rebase").split(" "))
                );
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s %s %s", type.toLowerCase(Locale.ROOT), action, keep).split(" "))
                );
                String escolha = dialog.getSelectedAction();
                String complemento = escolha.equalsIgnoreCase("Create merge request") ? " -o merge_request.create" : "";
                acaoPos = escolha.equalsIgnoreCase("Create merge request") ? "and merge request created" : "and pushed to " + GitFlowSettingsService.getInstance(project).getDevelopBranch();
                String cmd = "git push" + complemento;
                GitCommandExecutor.run(project, Arrays.asList(cmd.split(" ")));
            } catch (Exception ex) {
                NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                return;
            }
            NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Feature finished " + acaoPos + " successfully");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(GitFlowSettingsService.getInstance(project).getMainBranch()) &&
                branchName.startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())
        );
    }
}
