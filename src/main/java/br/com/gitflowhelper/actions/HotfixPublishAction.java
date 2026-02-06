package br.com.gitflowhelper.actions;

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
import git4idea.GitLocalBranch;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class HotfixPublishAction extends BaseAction {

    private static final String ACTION_DESCRIPTION = "Pushes the current hotfix branch to VCS (git flow hotfix publish).";;

    public HotfixPublishAction(Project project, String actionTitle, String type, String action, String branchName) {
        super(project, actionTitle, type, action, branchName, AllIcons.CodeWithMe.CwmShared, ACTION_DESCRIPTION);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                hotfixPublish();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Hotfix published successfully");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                StringUtil.isNotEmpty(getMainBranch()) &&
                branchName.startsWith(GitFlowSettingsService.getInstance(project).getHotfixPrefix())
        );
    }

    private List<GitResult> hotfixPublish() {
        List<GitResult> results = new ArrayList<>();
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        GitExecutor executor = new GitExecutor(project);

        for (GitRepository repository : repoManager.getRepositories()) {

            VirtualFile root = repository.getRoot();

            GitLocalBranch currentBranch = repository.getCurrentBranch();
            if (currentBranch == null) {
                throw new GitException("Não foi possível identificar a branch atual.");
            }

            String branchName = currentBranch.getName();

            // Validação básica de Git Flow
            if (!branchName.startsWith("hotfix/")) {
                throw new GitException(
                        "Branch atual não é uma hotfix: " + branchName
                );
            }

            // git push -u origin hotfix/<name>
            results.add(
                    executor.execute(
                            root,
                            GitCommand.PUSH,
                            "-u",
                            "origin",
                            branchName
                    )
            );

            // 🔄 Atualiza estado do repositório no IntelliJ
            repository.update();
        }

        return results;
    }
}
