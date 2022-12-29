package de.ithock.issuetracker.util

import com.intellij.credentialStore.CredentialAttributes
import java.util.*

object GitHubConnection {
    var accessToken: String? = null

    var username: String? = null
}

object SpaceConnection {
    var spaceUrl: String? = null
    var permanentToken: String? = null
    var clientId: String? = null
    var clientSecret: String? = null

    var accessToken: String? = null
    var accessTokenExpire: Date? = null
}

object Settings {
    var githubConnections: List<GitHubConnection> = listOf()
    var spaceConnections: List<SpaceConnection> = listOf()
}

class SettingsUtil {
    companion object {
        fun createCredentialAttributes(
            key: String,
            isPasswordMemoryOnly: Boolean = false,
            userName: String? = null
        ): CredentialAttributes {
            return CredentialAttributes(
                com.intellij.credentialStore.generateServiceName("de.ithock.issuetracker", key),
                userName?.takeIf { !isPasswordMemoryOnly },
                null,
                isPasswordMemoryOnly
            )
        }
    }
}