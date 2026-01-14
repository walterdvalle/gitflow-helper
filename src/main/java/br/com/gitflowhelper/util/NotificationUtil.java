package br.com.gitflowhelper.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class NotificationUtil {
    public static void showGitFlowSuccessNotification(Project project, String title, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("GitFlowNotificationGroup") // O ID do plugin.xml
                .createNotification(
                        title,
                        message,
                        NotificationType.INFORMATION
                )
                .notify(project);
    }

    public static void showGitFlowSErrorNotification(Project project, String title, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("GitFlowNotificationGroup") // O ID do plugin.xml
                .createNotification(
                        title,
                        message,
                        NotificationType.ERROR
                )
                .notify(project);
    }
}