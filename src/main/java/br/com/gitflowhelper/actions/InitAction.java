package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.InitDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class InitAction extends BaseAction {

    public InitAction(Project project, String actionTitle) {
        super(project, actionTitle, null, null, null, AllIcons.Actions.Lightning);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new InitDialog(project, this).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(StringUtil.isEmpty(getMainBranch()));
    }

    //invoked by InitDialog
    public void doOKAction(String mainField, String developField, String featureField,
                           String releaseField, String hotfixField) {
        String cmd1 = "git config gitflow.branch.master " + mainField;
        String cmd2 = "git config gitflow.branch.develop "+ developField;
        String cmd3 = "git flow init -d ";
        String cmd4 = "git config gitflow.prefix.feature " + featureField;
        String cmd5 = "git config gitflow.prefix.release " + releaseField;
        String cmd6 = "git config gitflow.prefix.hotfix " + hotfixField;
        try {
            GitCommandExecutor.run(this.project, List.of(cmd1.split(" ")));
            GitCommandExecutor.run(this.project, List.of(cmd2.split(" ")));
            GitCommandExecutor.run(this.project, List.of(cmd3.split(" ")));
            GitCommandExecutor.run(this.project, List.of(cmd4.split(" ")));
            GitCommandExecutor.run(this.project, List.of(cmd5.split(" ")));
            GitCommandExecutor.run(this.project, List.of(cmd6.split(" ")));
        } catch (GitException e) {
            NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
            return;
        }
        NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Git Flow Initialization Successful");
    }
}
