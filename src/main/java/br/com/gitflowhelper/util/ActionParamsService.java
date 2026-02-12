package br.com.gitflowhelper.util;

import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ActionParamsService implements PropertyChangeListener {

    private static final ActionParamsService INSTANCE = new ActionParamsService();
    private static Project project;
    private static String branchName;
    private static Map<AnAction, GitRepository> repos = new IdentityHashMap<AnAction, GitRepository>();
    private static Map<AnAction, String> names = new IdentityHashMap<AnAction, String>();

    private ActionParamsService() { }

    public static ActionParamsService getInstance() {
        return INSTANCE;
    }

    public static void setProject(Project project) {
        ActionParamsService.project = project;
    }
    public static void setBranchName(String branchName) {
        ActionParamsService.branchName = branchName;
    }

    public static Project getProject() { return project; }
    public static String getBranchName() {
        return branchName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        ActionParamsService.setBranchName((String) e.getNewValue());
        ActivityTracker.getInstance().inc();
    }

    public static void addRepo(AnAction action, GitRepository repo) {
        repos.put(action, repo);
    }
    public static GitRepository getRepo(AnAction action) {
        return repos.get(action);
    }

    public static void addName(AnAction action, String name) {
        names.put(action, name);
    }
    public static String getName(AnAction action) {
        return names.get(action);
    }

    public static void clearRepos() {
        repos.clear();
        repos = null;
    }
    public static void clearNames() {
        names.clear();
        names = null;
    }
}
