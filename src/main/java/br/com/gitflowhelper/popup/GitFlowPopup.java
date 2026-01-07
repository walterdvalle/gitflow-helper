package br.com.gitflowhelper.popup;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.dialog.InitDialog;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.toolwindow.ActionChoiceDialog;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.awt.RelativePoint;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public final class GitFlowPopup {

    private GitFlowPopup() {
        // utilitário estático
    }

    public static void show(Project project, Component component, int x, int y) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new AnAction("Init") {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new InitDialog(project).show();
            }
        });

        group.addSeparator();

        group.add(flowGroup(project, "Feature"));
        group.add(flowGroup(project, "Release"));
        group.add(flowGroup(project, "Hotfix"));

        JBPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        "Git Flow",
                        group,
                        DataManager.getInstance()
                                .getDataContextFromFocus().getResult()
                                ,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true
                );

        popup.show(new RelativePoint(component, new Point(x-100, y-140)));
    }

    private static DefaultActionGroup flowGroup(Project project, String type) {
        DefaultActionGroup group = new DefaultActionGroup(type, true);

        group.add(flowAction(project, type, "start"));
        group.add(flowAction(project, type, "publish"));
        group.add(flowAction(project, type, "finish"));

        return group;
    }

    private static AnAction flowAction(Project project, String type, String action) {
        String actionTitle =
                action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);

        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {

                if ("Feature".equals(type)) {
                    switch (action) {
                        case "start" -> new NameDialog(project, type + " start", "Feature description", name ->
                                GitCommandExecutor.run(
                                        project,
                                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                                )
                        ).show();
                        case "publish" -> GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                        );
                        case "finish" -> {
                            ActionChoiceDialog dialog = new ActionChoiceDialog(project);
                            if (dialog.showAndGet()) {
                                GitCommandExecutor.run(
                                        project,
                                        Arrays.asList(("git rebase").split(" "))
                                );

                                GitCommandExecutor.run(
                                        project,
                                        Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                                );
                                String escolha = dialog.getSelectedAction();
                                String complemento = escolha.equalsIgnoreCase("Create merge request") ? " -o merge_request.create": "";
                                String cmd = "git push"+complemento;
                                GitCommandExecutor.run(project, Arrays.asList(cmd.split(" ")));
                            }
                        }
                    }
                } else if ("Release".equals(type)) {
                    switch (action) {
                        case "start" -> new NameDialog(project, type + " start", "Version description", name ->
                                GitCommandExecutor.run(
                                        project,
                                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                                )
                        ).show();
                        case "publish" -> GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                        );
                        case "finish" -> {
                            GitCommandExecutor.run(
                                    project,
                                    Arrays.asList(String.format("git rebase origin/%s", GitFlowSettingsService.getInstance(project).getDevelopBranch()).split(" "))
                            );

                            GitCommandExecutor.run(
                                    project,
                                    Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                            );

                            GitCommandExecutor.run(project, Arrays.asList("git push".split(" ")));
                            GitCommandExecutor.run(project, Arrays.asList("git push --tags".split(" ")));
                        }
                    }
                } else if ("Hotfix".equals(type)) {
                    switch (action) {
                        case "start" -> new NameDialog(project, type + " start", "Hotfix description", name ->
                                GitCommandExecutor.run(
                                        project,
                                        Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                                )
                        ).show();
                        case "publish" -> GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                        );
                        case "finish" -> {
                            GitCommandExecutor.run(
                                    project,
                                    Arrays.asList(String.format("git rebase origin/%s", GitFlowSettingsService.getInstance(project).getDevelopBranch()).split(" "))
                            );

                            GitCommandExecutor.run(
                                    project,
                                    Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                            );

                            GitCommandExecutor.run(project, Arrays.asList("git push".split(" ")));
                        }
                    }
                }
            }
        };
    }
}
