package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.ActionChoiceDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class FeatureFinishAction extends BaseAction {

    public FeatureFinishAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String postAction;
        ActionChoiceDialog dialog = new ActionChoiceDialog(project, branchName, getDevelopBranch());

        try {
            String cmd = String.format("git log %s..%s --pretty=format:%%s%%n%%b", getDevelopBranch(), branchName);
            GitCommandExecutor.run(project,Arrays.asList((cmd).split(" ")));
        } catch (GitException ex) {
            NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
            return;
        }
        dialog.setLog(GitCommandExecutor.getLastMessage());

        if (dialog.showAndGet()) {
            String choice = dialog.getSelectedAction();

            List<String> gitCommit = new ArrayList<>();
            gitCommit.add("git");
            gitCommit.add("commit");
            for (String line : dialog.getCommitMessage().split("\\R")) {
                gitCommit.add("-m");
                gitCommit.add(line);
            }
            try {
//                GitCommandExecutor.run(project, Arrays.asList(("git rebase").split(" ")));
//                GitCommandExecutor.run(project,Arrays.asList(String.format("git flow %s %s %s", type.toLowerCase(Locale.ROOT), action, keep).split(" ")));
                if (choice.equalsIgnoreCase(ActionChoiceDialog.INTEGRATE)) {
                    GitCommandExecutor.run(project, Arrays.asList(("git fetch origin").split(" ")));
                    GitCommandExecutor.run(project, Arrays.asList(("git rebase origin/"+getDevelopBranch()).split(" ")));
                    GitCommandExecutor.run(project, Arrays.asList(("git checkout "+getDevelopBranch()).split(" ")));
                    GitCommandExecutor.run(project, Arrays.asList(("git merge --squash "+branchName).split(" ")));
                    GitCommandExecutor.run(project, gitCommit);
                    GitCommandExecutor.run(project, Arrays.asList(("git push origin "+getDevelopBranch()).split(" ")));
                    postAction = "Feature finished and pushed to " + getDevelopBranch() + " successfully.";
                } else {
                    //SELF_CREATE or AUTO_CREATE
                    GitCommandExecutor.run(project, Arrays.asList(("git fetch origin").split(" ")));
                    GitCommandExecutor.run(project, Arrays.asList(("git rebase origin/"+getDevelopBranch()).split(" ")));
                    if (choice.equalsIgnoreCase(ActionChoiceDialog.SELF_CREATE)) {
                        GitCommandExecutor.run(project, Arrays.asList(("git push origin "+branchName+" --force-with-lease").split(" ")));
                        postAction = "Feature pushed to " + branchName + " successfully. Create yourself a merge/pull request.";
                    } else {
                        //AUTO_CREATE
                        GitCommandExecutor.run(project, Arrays.asList("git", "commit", "--allow-empty", "-m", "trigger MR"));
                        List<String> pushCreateCommand = formatCommand(dialog.getCommitMessage());
                        GitCommandExecutor.run(project, pushCreateCommand);
                        postAction = "Feature pushed to " + branchName + " and merge request created successfully.";
                    }
                    GitCommandExecutor.run(project, Arrays.asList(("git checkout "+getDevelopBranch()).split(" ")));
                }

                if (!dialog.getKeepLocalBranch()) {
                    GitCommandExecutor.run(project, Arrays.asList(("git branch -D " + branchName).split(" ")));
                }
                if (!dialog.getKeepRemoteBranch()) {
                    if (choice.equalsIgnoreCase(ActionChoiceDialog.INTEGRATE)) {
                        GitCommandExecutor.run(project, Arrays.asList(("git push origin --delete "+branchName).split(" ")));
                        GitCommandExecutor.run(project, Arrays.asList(("git push origin "+getDevelopBranch()).split(" ")));
                    }
                }
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                return;
            }
            NotificationUtil.showGitFlowSuccessNotification(project, "Success",  postAction);
        }
    }

    private @NotNull List<String> formatCommand(String commitMessage) {
        String title = commitMessage.substring(0, commitMessage.indexOf("\n"));
        StringBuilder description = new StringBuilder();
        for (String line : commitMessage.substring(commitMessage.indexOf("\n")+1).split("\\R")) {
            description.append(line).append("<br/>");
        }
        List<String> cmd = new ArrayList<>();
        cmd.addAll(Arrays.asList(("git push origin "+branchName+" -o merge_request.create").split(" ")));
        cmd.add("-o");
        cmd.add("merge_request.target="+getDevelopBranch());
        cmd.add("-o");
        cmd.add("merge_request.title="+title);
        cmd.add("-o");
        cmd.add("merge_request.description="+description);
        return cmd;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                branchName.startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())
        );
    }
}
