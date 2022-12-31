package de.ithock.advancedissuetracker.util

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection

class PasswordSafeUtil {
    companion object {
        fun getCredentialAttributes(uniqueId: String): CredentialAttributes {
            return CredentialAttributes(
                "de.ithock.advancedissuetracker:${uniqueId}",
                null,
                PasswordSafeUtil::class.java,
                true
            )
        }
        fun getCredentialAttributes(connection: IssueTrackerConnection): CredentialAttributes {
            return getCredentialAttributes(connection.uniqueId)
        }
        fun getCredentialsForConnection(connection: IssueTrackerConnection): Credentials? {
            return PasswordSafe.instance.get(
                getCredentialAttributes(connection)
            )
        }

        fun getCredentialsForConnection(uniqueId:String): Credentials? {
            return PasswordSafe.instance.get(
                getCredentialAttributes(uniqueId)
            )
        }

        fun setCredentialsForConnection(connection: IssueTrackerConnection, credentials: Credentials? = null) {
            PasswordSafe.instance.set(
                getCredentialAttributes(connection),
                credentials?:connection.credentials
            )
        }
    }
}