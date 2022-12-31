package de.ithock.issuetracker.util

import ai.grazie.utils.mpp.URLEncoder
import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import de.ithock.issuetracker.PluginBundle
import java.text.SimpleDateFormat
import java.util.*
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.PropertyKey


class Helpers {
    companion object {
        fun format(format: Date): String {
            return SimpleDateFormat("dd MMM yyyy HH:mm").format(format)
        }

        fun asAgo(date: Date): String {
            val diff = Date().time - date.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            return when {
                seconds < 60 -> PluginBundle.get("ago.one_second")
                minutes < 60 -> PluginBundle.get("ago.x_minutes", minutes)
                hours < 24 -> PluginBundle.get("ago.x_hours", hours)
                days < 7 -> PluginBundle.get("ago.x_days", days)
                weeks < 4 -> PluginBundle.get("ago.x_weeks", weeks)
                months < 12 -> PluginBundle.get("ago.x_months", months)
                else -> PluginBundle.get("ago.x_years", years)
            }
        }

        fun getLogger(): Logger {
            return Logger.getInstance("de.ithock.issuetracker")
        }

        fun showNotification(
            @PropertyKey(resourceBundle = "messages.main") title: String,
            @PropertyKey(resourceBundle = "messages.main") message: String,
            vararg params: Any,
            type: NotificationType,
            actions: List<NotificationAction> = emptyList(),
            notificationGroup: String = "de.ithock.issuetracker.notification",
            project: Project? = null
        ): Notification {
            val notification = Notification(
                notificationGroup, PluginBundle.get(title), PluginBundle.get(message, *params), type
            )
            actions.forEach { notification.addAction(it) }
            notification.notify(project)
            return notification
        }

        fun showErrorNotification(
            @PropertyKey(resourceBundle = "messages.main") title: String,
            @PropertyKey(resourceBundle = "messages.main") message: String,
            vararg params: Any,
            project: Project? = null,
            actions: List<NotificationAction> = emptyList(),
            retryAction: Runnable? = null
        ): Notification {
            val contactSupportAction = NotificationAction.createSimple(
                PluginBundle.get("notification.contact_support")
            ) {
                BrowserUtil.browse(PluginBundle.get("support.url"))
            }
            val retryNotificationAction = retryAction?.let {
                NotificationAction.createSimpleExpiring(PluginBundle.get("notification.retry")) {
                    it.run()
                }
            }
            return showNotification(
                title,
                message,
                *params,
                type = NotificationType.ERROR,
                actions = actions + listOfNotNull(contactSupportAction, retryNotificationAction),
                notificationGroup = "de.ithock.issuetracker.error",
                project = project
            )
        }
    }
}