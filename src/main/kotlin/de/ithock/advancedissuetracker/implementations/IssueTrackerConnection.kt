package de.ithock.advancedissuetracker.implementations

import com.intellij.credentialStore.Credentials
import java.util.UUID

abstract class IssueTrackerConnection(
    /**
     * The base URL of the connection, e.g. https://space.example.com
     */
    var url: String,

    /**
     * A UUID that identifies the connection.
     */
    var uniqueId: String = UUID.nameUUIDFromBytes(url.toByteArray()).toString(),

    var credentials: Credentials? = Credentials("", ""),
) {
    override fun toString(): String {
        return "IssueTrackerConnection(url='$url', uniqueId='$uniqueId')"
    }

    abstract fun getAvatarUrl(avatar: String): String

    enum class Type {
        SPACE,
        JIRA,
        GITHUB,
        GITLAB,
        BITBUCKET
    }
}