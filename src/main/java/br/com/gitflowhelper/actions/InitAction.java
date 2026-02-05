package br.com.gitflowhelper.actions;

import br.com.gitflowhelper.dialog.InitDialog;
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
import git4idea.GitLocalBranch;
import git4idea.commands.GitCommand;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class InitAction extends BaseAction {

    private static final String ACTION_DESCRIPTION = "Initializes current repo to use Git Flow (git flow init).";

    public InitAction(Project project, String actionTitle) {
        super(project, actionTitle, null, null, null, AllIcons.Scope.Production, ACTION_DESCRIPTION);
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
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            setLoading(true);
            try {
                init(true);
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Git Flow Initialization Successful");
            } catch (GitException ex) {
                NotificationUtil.showGitFlowErrorNotification(project, "Error", ex.getGitResult().getProcessMessage());
            }
            setLoading(false);
        });

//        String cmd1 = "git config gitflow.branch.master " + mainField;
//        String cmd2 = "git config gitflow.branch.develop "+ developField;
//        String cmd3 = "git flow init -d ";
//        String cmd4 = "git config gitflow.prefix.feature " + featureField;
//        String cmd5 = "git config gitflow.prefix.release " + releaseField;
//        String cmd6 = "git config gitflow.prefix.hotfix " + hotfixField;
//        try {
//            GitCommandExecutor.run(this.project, List.of(cmd1.split(" ")));
//            GitCommandExecutor.run(this.project, List.of(cmd2.split(" ")));
//            GitCommandExecutor.run(this.project, List.of(cmd3.split(" ")));
//            GitCommandExecutor.run(this.project, List.of(cmd4.split(" ")));
//            GitCommandExecutor.run(this.project, List.of(cmd5.split(" ")));
//            GitCommandExecutor.run(this.project, List.of(cmd6.split(" ")));
//        } catch (GitException e) {
//            NotificationUtil.showGitFlowErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
//            return;
//        }
//        NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Git Flow Initialization Successful");
    }

    public List<GitResult> init(boolean pushOnFinish) {

        List<GitResult> results = new ArrayList<>();
        GitExecutor executor = new GitExecutor(project);
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);

        GitFlowSettingsService settings = GitFlowSettingsService.getInstance(project);
        String mainBranch = settings.getMainBranch();
        String developBranch = settings.getDevelopBranch();
        String featurePrefix = normalizePrefix(settings.getFeaturePrefix());
        String releasePrefix = normalizePrefix(settings.getReleasePrefix());
        String hotfixPrefix  = normalizePrefix(settings.getHotfixPrefix());

        for (GitRepository repository : repoManager.getRepositories()) {

            VirtualFile root = repository.getRoot();

            // 🔎 does local branches exist
            Map<String, GitLocalBranch> localBranches =
                    repository.getBranches()
                            .getLocalBranches()
                            .stream()
                            .collect(Collectors.toMap(
                                    GitLocalBranch::getName,
                                    Function.identity()
                            ));

            // 1 main branch
            if (!localBranches.containsKey(mainBranch)) {
                throw new GitException(
                        "Main branch '" + mainBranch + "' does not exist in this repo." +
                                root.getPath()
                );
            }

            // 2 create develop if needed
            if (!localBranches.containsKey(developBranch)) {

                results.add(
                        executor.execute(
                                root,
                                GitCommand.CHECKOUT,
                                mainBranch
                        )
                );

                results.add(
                        executor.execute(
                                root,
                                GitCommand.CHECKOUT,
                                "-b",
                                developBranch
                        )
                );
            }

            // 3 go to develop
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CHECKOUT,
                            developBranch
                    )
            );

            // 4 initial push  (opcional)
            if (pushOnFinish) {
                results.add(
                        executor.execute(
                                root,
                                GitCommand.PUSH,
                                "-u",
                                "origin",
                                developBranch
                        )
                );
            }

            // 5 git config - gitflow settings
            results.add(
                    executor.execute(
                            root,
                            GitCommand.CONFIG,
                            "gitflow.branch.master",
                            mainBranch
                    )
            );

            results.add(
                    executor.execute(
                            root,
                            GitCommand.CONFIG,
                            "gitflow.branch.develop",
                            developBranch
                    )
            );

            results.add(
                    executor.execute(
                            root,
                            GitCommand.CONFIG,
                            "gitflow.prefix.feature",
                            featurePrefix
                    )
            );

            results.add(
                    executor.execute(
                            root,
                            GitCommand.CONFIG,
                            "gitflow.prefix.release",
                            releasePrefix
                    )
            );

            results.add(
                    executor.execute(
                            root,
                            GitCommand.CONFIG,
                            "gitflow.prefix.hotfix",
                            hotfixPrefix
                    )
            );

            // update repo state in intellij
            repository.update();
        }

        return results;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
}
