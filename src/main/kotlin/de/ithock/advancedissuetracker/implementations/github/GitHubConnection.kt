package de.ithock.advancedissuetracker.implementations.github

import com.intellij.credentialStore.Credentials
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import java.util.*

class GitHubConnection(
    url: String,
    uniqueId: String = UUID.nameUUIDFromBytes(url.toByteArray()).toString(),
    credentials: Credentials? = null,
) : IssueTrackerConnection(url, uniqueId, credentials) {
    override fun getAvatarUrl(avatar: String): String {
        TODO("Not yet implemented")
    }
}