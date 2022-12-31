package de.ithock.advancedissuetracker.implementations.space

import com.intellij.credentialStore.Credentials
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import java.util.*

class SpaceConnection(
    url: String,
    uniqueId: String = UUID.nameUUIDFromBytes(url.toByteArray()).toString(),
    val projectId: String = "",
    credentials: Credentials? = null
) : IssueTrackerConnection(url, uniqueId, credentials) {

    private val accessToken: String = credentials?.password.toString()

    fun getAccessToken(): String {
        return accessToken
    }

    override fun getAvatarUrl(avatar: String): String {
        return "$url/d/$avatar"
    }
}