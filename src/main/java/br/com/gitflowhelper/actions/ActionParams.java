package br.com.gitflowhelper.actions;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;

import javax.swing.*;

@SuppressWarnings("unused")
public record ActionParams(
        Project project,
        String type,
        String action,
        String branchName,
        String postAction,
        String featureCommits,
        GitRepository repository,
        boolean isCurrent,
        JTextPane textPane) {

    //Others
    public ActionParams(Project project,
                        String type,
                        String action,
                        String branchName) {
        this(project, type, action, branchName, null, null, null, false, null);
    }

    //FeatureFinishAction
    public ActionParams(Project project,
                        String type,
                        String action,
                        String branchName,
                        String postAction,
                        String featureCommits) {
        this(project, type, action, branchName, postAction, featureCommits, null, false, null);
    }

    //CheckoutLocalBranchAction CheckoutRemoteBranchAction
    public ActionParams(Project project,
                        String type,
                        String action,
                        String branchName,
                        GitRepository repository,
                        boolean isCurrent) {
        this(project, type, action, branchName, null, null, repository, isCurrent, null);
    }

    //ClearToolWindowAction
    public ActionParams(JTextPane textPane) {
        this(null, null, null, null, null, null, null, false, textPane);
    }
}
