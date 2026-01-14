package br.com.gitflowhelper.util;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSafeAction extends AnAction {

    public BaseSafeAction(String actionTitle) {
        super(actionTitle);
    }

    protected abstract void safeActionPerformed(@NotNull AnActionEvent e) throws Exception;

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        try {
            safeActionPerformed(e);
        } catch (Throwable ex) {
            handleGlobalException(ex, e);
        }
    }

    private void handleGlobalException(Throwable ex, @NotNull AnActionEvent e) {
        NotificationUtil.showGitFlowSErrorNotification(e.getProject(), "Error", ex.getMessage());
    }
}