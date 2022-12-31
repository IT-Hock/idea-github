package de.ithock.issuetracker.util

import com.intellij.openapi.util.IconLoader

object Icons {
    @JvmField
    val Space = IconLoader.getIcon("/icons/Space_icon.svg", javaClass)
    @JvmField
    val YouTrack = IconLoader.getIcon("/icons/YouTrack_icon.svg", javaClass)
    @JvmField
    val Loading = IconLoader.getIcon("/icons/Loading.svg", javaClass)
    @JvmField
    val IssueClosed = IconLoader.getIcon("/icons/issueClosed.svg", javaClass)
    @JvmField
    val IssueOpen = IconLoader.getIcon("/icons/issueOpen.svg", javaClass)
    @JvmField
    val DefaultAvatar = IconLoader.getIcon("/icons/defaultAvatar.svg", javaClass)
}