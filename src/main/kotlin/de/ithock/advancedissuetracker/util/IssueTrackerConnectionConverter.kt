package de.ithock.advancedissuetracker.util

import com.intellij.util.xmlb.Converter
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import de.ithock.advancedissuetracker.implementations.space.SpaceConnection
import org.json.JSONObject

class IssueTrackerConnectionConverter : Converter<IssueTrackerConnection>() {
    override fun toString(value: IssueTrackerConnection): String {
        PasswordSafeUtil.setCredentialsForConnection(value)
        return JSONObject(value).toString()
    }

    override fun fromString(value: String): IssueTrackerConnection? {
        val json = JSONObject(value)
        val type = json.getEnum(IssueTrackerConnection.Type::class.java, "type") ?: return null
        val uniqueId = json.getString("uniqueId") ?: return null
        val url = json.getString("url") ?: return null
        val connection: IssueTrackerConnection = when(type) {
            IssueTrackerConnection.Type.SPACE -> {
                val projectKey = json.getString("projectKey") ?: return null
                val credentials = PasswordSafeUtil.getCredentialsForConnection(uniqueId)
                val con = SpaceConnection(url, uniqueId, projectKey, credentials)
                con
            }
            IssueTrackerConnection.Type.JIRA -> TODO()
            IssueTrackerConnection.Type.GITHUB -> TODO()
            IssueTrackerConnection.Type.GITLAB -> TODO()
            IssueTrackerConnection.Type.BITBUCKET -> TODO()
        }
        return connection
    }
}