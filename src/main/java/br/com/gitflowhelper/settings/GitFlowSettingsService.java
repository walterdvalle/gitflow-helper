package br.com.gitflowhelper.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(
        name = "GitFlowSettings",
        storages = @Storage("gitflow-helper.xml")
)
public class GitFlowSettingsService
        implements PersistentStateComponent<GitFlowSettingsState> {

    private GitFlowSettingsState state = new GitFlowSettingsState();

    @Override
    public GitFlowSettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull GitFlowSettingsState state) {
        this.state = state;
    }

    public static GitFlowSettingsService getInstance(Project project) {
        return project.getService(GitFlowSettingsService.class);
    }

    public String getFeaturePrefix() {
        return state.getFeaturePrefix();
    }

    public void setFeaturePrefix(String featurePrefix) {
        state.setFeaturePrefix(featurePrefix);
    }

    public String getReleasePrefix() {
        return state.getReleasePrefix();
    }

    public void setReleasePrefix(String releasePrefix) {state.setReleasePrefix(releasePrefix);}

    public String getHotfixPrefix() {
        return state.getHotfixPrefix();
    }

    public void setHotfixPrefix(String hotfixPrefix) {
        state.setHotfixPrefix(hotfixPrefix);
    }

    public String getMainBranch() {return state.getMainBranch();}

    public void setMainBranch(String mainBranch) {state.setMainBranch(mainBranch);}

    public String getDevelopBranch() {return state.getDevelopBranch();}

    public void setDevelopBranch(String developBranch) {state.setDevelopBranch(developBranch);}
}
