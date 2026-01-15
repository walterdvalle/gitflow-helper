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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public final class GitFlowPopup {
    private ListPopup listPopup;
    private final Project project;
    private String branchName;

    public GitFlowPopup(Project project) {
        this.branchName = "";
        this.project = project;

        DataManager.getInstance()
            .getDataContextFromFocusAsync()
            .onSuccess(dataContext -> {

                this.listPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                        "Git Flow",
                        createGroup(),
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true
                );
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    //slow job
                    this.branchName = GitBranchUtils.getCurrentBranchName(project);
                    ApplicationManager.getApplication().invokeLater(this::updateUI);
                });

                this.listPopup.addListener(new JBPopupListener() {
                       @Override
                       public void beforeShown(@NotNull LightweightWindowEvent event) {
                           var oldPlace = listPopup.getLocationOnScreen();
                           var newPlace = new Point((int) oldPlace.getX(), (int) oldPlace.getY()+40);
                           listPopup.setLocation(newPlace);
                           JBPopupListener.super.beforeShown(event);
                       }
                   }
                );
            });
    }

    private void updateUI() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            statusBar.updateWidget("GitFlowHelper");
        }
    }

    private DefaultActionGroup createGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(init());
        group.addSeparator();
        group.add(flowGroup("Feature"));
        group.add(flowGroup("Release"));
        group.add(flowGroup("Hotfix" ));
        return group;
    }

    public ListPopup getPopup() {
        return this.listPopup;
    }

    private DefaultActionGroup flowGroup(String type) {
        DefaultActionGroup group = new DefaultActionGroup(type, true);
        group.add(flowAction(type, "start"));
        group.add(flowAction(type, "publish"));
        group.add(flowAction(type, "finish"));
        return group;
    }

    private AnAction flowAction(String type, String action) {
        String actionTitle = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1);
        if ("Feature".equals(type)) {
            switch (action) {
                case "start":
                    return featureStart(type, actionTitle);
                case "publish":
                    return featurePublish(type, action, actionTitle);
                case "finish":
                    return featureFinish(type, action, actionTitle);
            }
        } else if ("Release".equals(type)) {
            switch (action) {
                case "start":
                    return releaseStart(type, actionTitle);
                case "publish":
                    return releasePublish(type, action, actionTitle);
                case "finish":
                    return releaseFinish(type, action, actionTitle);
            }
        } else if ("Hotfix".equals(type)) {
            switch (action) {
                case "start":
                    return hotfixStart(type, actionTitle);
                case "publish":
                    return hotfixPublish(type, action, actionTitle);
                case "finish":
                    return hotfixFinish(type, action, actionTitle);
            }
        }
        return null;
    }

    private @NotNull AnAction init() {
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

    private @NotNull AnAction featureStart(String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Feature description", name ->
                {
                    try {
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                        );
                    } catch (Exception ex) {
                        NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                        return;
                    }
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature created successfully");
                }
                ).show();
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.equals(GitFlowSettingsService.getInstance(project).getDevelopBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction featurePublish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                try {
                    GitCommandExecutor.run(
                            project,
                            Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                    );
                } catch (Exception ex) {
                    NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                    return;
                }
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New feature published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction featureFinish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                ActionChoiceDialog dialog = new ActionChoiceDialog(project);
                String acaoPos = "";
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
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getFeaturePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction releaseStart(String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Version description", name ->
                {
                    try {
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "-")).split(" "))
                        );
                    } catch (Exception ex) {
                        NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                        return;
                    }
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release created successfully");
                }
                ).show();
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.equals(GitFlowSettingsService.getInstance(project).getMainBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction releasePublish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                try {
                    GitCommandExecutor.run(
                            project,
                            Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                    );
                } catch (Exception ex) {
                    NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                    return;
                }
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New release published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getReleasePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction releaseFinish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                try {
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
                } catch (Exception ex) {
                    NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                    return;
                }

                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Released finished and tag pushed successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getReleasePrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction hotfixStart(String type, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                new NameDialog(project, type + " start", "Hotfix description", name ->
                {
                    try {
                        GitCommandExecutor.run(
                                project,
                                Arrays.asList(String.format("git flow %s start %s", type.toLowerCase(Locale.ROOT), name.replaceAll(" ", "_")).split(" "))
                        );
                    } catch (Exception ex) {
                        NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                        return;
                    }
                    NotificationUtil.showGitFlowSuccessNotification(project, "Success", "New hotfix created successfully");
                }
                ).show();
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.equals(GitFlowSettingsService.getInstance(project).getMainBranch())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction hotfixPublish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                try {
                    GitCommandExecutor.run(
                            project,
                            Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                    );
                } catch (Exception ex) {
                    NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                    return;
                }
                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Hotfix published successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getHotfixPrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }

    private @NotNull AnAction hotfixFinish(String type, String action, String actionTitle) {
        return new AnAction(actionTitle) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                try {
                    GitCommandExecutor.run(
                            project,
                            Arrays.asList(String.format("git rebase origin/%s", GitFlowSettingsService.getInstance(project).getDevelopBranch()).split(" "))
                    );
                    GitCommandExecutor.run(
                            project,
                            Arrays.asList(String.format("git flow %s %s", type.toLowerCase(Locale.ROOT), action).split(" "))
                    );
                    GitCommandExecutor.run(project, Arrays.asList("git push".split(" ")));
                } catch (Exception ex) {
                    NotificationUtil.showGitFlowSErrorNotification(project, "Error", GitCommandExecutor.getLastErrorMessage());
                    return;
                }

                NotificationUtil.showGitFlowSuccessNotification(project, "Success", "Hotfix finished and tag pushed successfully");
            }
            @Override
            public void update(AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                if (branchName.startsWith(GitFlowSettingsService.getInstance(project).getHotfixPrefix())) {
                    presentation.setEnabled(true);
                } else {
                    presentation.setEnabled(false);
                }
            }
        };
    }
}
