package br.com.gitflowhelper.popup;

import br.com.gitflowhelper.dialog.NameDialog;
import br.com.gitflowhelper.util.GitBranchUtils;
import br.com.gitflowhelper.util.NotificationUtil;
import br.com.gitflowhelper.git.GitCommandExecutor;
import br.com.gitflowhelper.dialog.InitDialog;
import br.com.gitflowhelper.settings.GitFlowSettingsService;
import br.com.gitflowhelper.dialog.ActionChoiceDialog;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public final class GitFlowPopup {

    private GitFlowPopup() {}

    public static void show(Project project, Component component, int x, int y) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(init(project));
        group.addSeparator();
        group.add(flowGroup(project, "Feature"));
        group.add(flowGroup(project, "Release"));
        group.add(flowGroup(project, "Hotfix"));

        JBPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
            "Git Flow",
            group,
            DataManager.getInstance().getDataContextFromFocus().getResult(),
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
        String actionTitle = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);
        if ("Feature".equals(type)) {
            switch (action) {
                case "start":
                    return featureStart(project, type, actionTitle);
                case "publish":
                    return featurePublish(project, type, action, actionTitle);
                case "finish":
                    return featureFinish(project, type, action, actionTitle);
            }
        } else if ("Release".equals(type)) {
            switch (action) {
                case "start":
                    return releaseStart(project, type, actionTitle);
                case "publish":
                    return releasePublish(project, type, action, actionTitle);
                case "finish":
                    return releaseFinish(project, type, action, actionTitle);
            }
        } else if ("Hotfix".equals(type)) {
            switch (action) {
                case "start":
                    return hotfixStart(project, type, actionTitle);
                case "publish":
                    return hotfixPublish(project, type, action, actionTitle);
                case "finish":
                    return hotfixFinish(project, type, action, actionTitle);
            }
        }
        return null;
    }

    private static @NotNull AnAction init(Project project) {
        return new AnAction("Init") {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new InitDialog(project).show();
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitFlowSettingsService.getInstance(project).getMainBranch() == null) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction featureStart(Project project, String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Feature description", name ->
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                        )
                ).show();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature created successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).equals(GitFlowSettingsService.getInstance(project).getDevelopBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction featurePublish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                );
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction featureFinish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                ActionChoiceDialog dialog = new ActionChoiceDialog(project);
                if (dialog.showAndGet()) {
                    String keep = dialog.getKeepBranch() ? " --keep" : "";
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
                    String acaoPos = escolha.equalsIgnoreCase("Create merge request") ? "and merge request created" : "and pushed to " + GitFlowSettingsService.getInstance(project).getDevelopBranch();
                    String cmd = "git push" + complemento;
                    GitCommandExecutor.run(project, Arrays.asList(cmd.split(" ")));
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Feature finished " + acaoPos + " successfully");
                }
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction releaseStart(Project project, String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Version description", name ->
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                        )
                ).show();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release created successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).equals(GitFlowSettingsService.getInstance(project).getMainBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction releasePublish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                );
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getReleasePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction releaseFinish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
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
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Released finished and tag pushed successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getReleasePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction hotfixStart(Project project, String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Hotfix description", name ->
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "_")).split(" "))
                        )
                ).show();
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New hotfix created successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).equals(GitFlowSettingsService.getInstance(project).getMainBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction hotfixPublish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                );
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Hotfix published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getHotfixPrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private static @NotNull AnAction hotfixFinish(Project project, String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git rebase origin/%s", GitFlowSettingsService.getInstance(project).getDevelopBranch()).split(" "))
                );

                GitCommandExecutor.run(
                        project,
                        Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                );

                GitCommandExecutor.run(project, Arrays.asList("git push".split(" ")));
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Hotfix finished and tag pushed successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (GitBranchUtils.getCurrentBranchName(project).startsWith(GitFlowSettingsService.getInstance(project).getHotfixPrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }
}
