package br.com.gitflowhelper.actions;

import com.intellij.openapi.project.Project;

public final class ActionParamsService {

    private static final ActionParamsService INSTANCE = new ActionParamsService();
    private static Project project;
    private static String branchName;

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
    public static Project getProject() {
        return project;
    }
    public static String getBranchName() {
        return branchName;
    }


}
