package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.*;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.NotificationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class FeatureStartAction extends BaseAction {

    public FeatureStartAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Actions.Execute);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        new NameDialog(project, type + " start", "Feature description", name ->
        {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                setLoading(true);
                try {
                    featureStart(project, name);
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature created successfully");
                } catch (GitException ex) {
                    NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
                }
                setLoading(false);
            });
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

    private List<GitResult> featureStart(Project project, String featureName) {
        String featureBranch = GitFlowSettingsService.getInstance(project).getFeaturePrefix() + featureName;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);
        List<GitResult> results = new ArrayList<>();

        for (GitRepository repository : repoManager.getRepositories()) {
            VirtualFile root = repository.getRoot();

            // 1. checkout develop
            results.add(
                    executor.execute(root, GitCommand.CHECKOUT, GitFlowSettingsService.getInstance(project).getDevelopBranch())
            );

            // 2. pull develop
            results.add(
                    executor.execute(root, GitCommand.PULL)
            );

            // 3 + 4. create + checkout feature branch
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            "-b",
                            featureBranch,
                            GitFlowSettingsService.getInstance(project).getDevelopBranch()
                    )
            );

            repository.update();
        }
        return results;
    }

}
