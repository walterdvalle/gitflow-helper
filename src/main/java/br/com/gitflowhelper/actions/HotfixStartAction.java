package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.git.GitException;
import br.com.gitflowhelper.git.GitExecutor;
import br.com.gitflowhelper.git.GitResult;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class HotfixStartAction extends BaseAction {

    private static final String ACTION_DESCRIPTION = "Creates a new local hotfix branch (git flow hotfix start).";;

    public HotfixStartAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.Actions.Execute, ACTION_DESCRIPTION);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new NameDialog(project, type + " start", "Hotfix description", true, name ->
        {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                setLoading(true);
                try {
                    hotfixStart(name.getName(), name.getPushOnFinish());
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New hotfix created successfully");
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
                branchName.equals(getMainBranch())
        );
    }

    private List<GitResult> hotfixStart(String hotfixName, boolean pushOnFinish) {
        List<GitResult> results = new ArrayList<>();
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);

        String mainBranch = GitFlowSettingsService.getInstance(project).getMainBranch();
        String hotfixBranch = GitFlowSettingsService.getInstance(project).getHotfixPrefix() + hotfixName;

        for (GitRepository repository : repoManager.getRepositories()) {

            VirtualFile root = repository.getRoot();

            // 1) checkout main
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            mainBranch
                    )
            );

            // 2) pull main
            results.add(
                    executor.execute(
                            root,
                            GitCommand.PULL,
                            "origin",
                            mainBranch
                    )
            );

            // 3) create hotfix branch from main
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            "-b",
                            hotfixBranch,
                            mainBranch
                    )
            );

            // 4) push hotfix (if needed)
            if (pushOnFinish) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.PUSH,
                                "-u",
                                "origin",
                                hotfixBranch
                        )
                );
            }

            repository.update();
        }

        return results;
    }
}
