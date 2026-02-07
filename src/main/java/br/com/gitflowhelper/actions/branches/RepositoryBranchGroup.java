package br.com.gitflowhelper.actions.branches;

import br.com.gitflowhelper.actions.BaseAction;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.util.GitFlowDescriptions;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@Deprecated
public class RepositoryBranchGroup extends DefaultActionGroup {

    public RepositoryBranchGroup(Project project, GitRepository repository) {
        super(repository.getProject().getName(), GitFlowDescriptions.REPO_GROUP.getValue(), AllIcons.Actions.CheckOut);
        setPopup(true);

        String currentBranch = repository.getCurrentBranchName();

        add(new AnAction("Local Branches", "", AllIcons.Actions.MenuOpen) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(false);
                presentation.setWeight(Presentation.HIGHER_WEIGHT);
            }
        });
        addSeparator();

        List<GitLocalBranch> orderedLocalBranches = sortBranches(
                repository.getBranches().getLocalBranches(),
                currentBranch,
                GitLocalBranch::getName,
                project
        );

        orderedLocalBranches.forEach(branch -> {
            boolean isCurrent = branch.getName().equals(currentBranch);

            add(new CheckoutLocalBranchAction(
                    project,
                    repository,
                    branch.getName(),
                    isCurrent
            ));
        });

        addSeparator();
        add(new AnAction("Remote Branches", "", AllIcons.Actions.MenuOpen) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(false);
                presentation.setWeight(Presentation.EVEN_HIGHER_WEIGHT);
            }
        });
        addSeparator();

        List<GitRemoteBranch> orderedRemoteBranches = sortBranches(
                repository.getBranches().getRemoteBranches(),
                BaseAction.REMOTE+"/"+currentBranch,
                GitRemoteBranch::getName,
                project
        );
        orderedRemoteBranches.forEach(branch -> {
            boolean isCurrent = branch.getName().equals(BaseAction.REMOTE+"/"+currentBranch);
            add(new CheckoutRemoteBranchAction(
                    project,
                    repository,
                    branch.getName(),
                    isCurrent
            ));
        });

    }

    private <T> List<T> sortBranches(
            Collection<T> branches,
            String currentBranchName,
            Function<T, String> branchNameExtractor,
            Project project) {
        GitFlowSettingsService service = GitFlowSettingsService.getInstance(project);
        Map<String, T> byName = new HashMap<>();
        for (T branch : branches) {
            byName.put(branchNameExtractor.apply(branch), branch);
        }

        List<T> result = new ArrayList<>();

        // 1) current branch
        if (currentBranchName != null && byName.containsKey(currentBranchName)) {
            result.add(byName.remove(currentBranchName));
        }

        // 2) main
        if (byName.containsKey(service.getMainBranch())) {
            result.add(byName.remove(service.getMainBranch()));
        }
        if (byName.containsKey(BaseAction.REMOTE + "/" + service.getMainBranch())) {
            result.add(byName.remove(BaseAction.REMOTE + "/" + service.getMainBranch()));
        }

        // 3) develop
        if (byName.containsKey(service.getDevelopBranch())) {
            result.add(byName.remove(service.getDevelopBranch()));
        }
        if (byName.containsKey(BaseAction.REMOTE + "/" + service.getDevelopBranch())) {
            result.add(byName.remove(BaseAction.REMOTE + "/" + service.getDevelopBranch()));
        }


        // 4) everything else
        byName.values().stream()
                .sorted(Comparator.comparing(branchNameExtractor, String.CASE_INSENSITIVE_ORDER))
                .forEach(result::add);

        return result;
    }

}
