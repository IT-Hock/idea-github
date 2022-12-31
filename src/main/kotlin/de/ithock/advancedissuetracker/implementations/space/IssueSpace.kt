package de.ithock.advancedissuetracker.implementations.space

import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import de.ithock.advancedissuetracker.implementations.IssueUser
import org.json.JSONObject
import java.util.*

/**
 * Issue class for JetBrains Space
 * extends the Issue class from the issuetracker library
 */
class IssueSpace() : Issue() {
    private var dueDate: Date? = null
    private var tags: List<String> = emptyList()


    constructor(connection: SpaceConnection? = null, jsonIssue: JSONObject) : this() {

        identifier = jsonIssue.getString("id")
        summary = jsonIssue.getString("title")
        body = jsonIssue.getString("description")
        /**
        "status": {
        "id": "2eeY1E3RD2tu",
        "archived": false,
        "name": "Open",
        "resolved": false,
        "color": "1965b0"
        },
         */
        state = IssueStatusSpace(jsonIssue.getJSONObject("status"))
        author = IssueSpaceUser(jsonIssue.getJSONObject("createdBy").getJSONObject("details").getJSONObject("user"))
        (author as IssueSpaceUser).setValuesFromConnection(connection)
        if (jsonIssue.has("assignee")) {
            assignee = IssueSpaceUser(jsonIssue.getJSONObject("assignee"))
            (assignee as IssueSpaceUser).setValuesFromConnection(connection)
        }
        createdAt = Date(jsonIssue.getJSONObject("creationTime").getLong("timestamp"))

        // Parse date from ISO
        // YYYY-MM-DD
        val dueDate = jsonIssue.optString("dueDate")
        if (dueDate != null && dueDate.isNotEmpty()) {
            val parsedDate = dueDate.split("-")
            if (parsedDate.size == 3) {
                this.dueDate = Date(parsedDate[0].toInt() - 1900, parsedDate[1].toInt() - 1, parsedDate[2].toInt())
            }
        }
        tags = jsonIssue.getJSONArray("tags").map { it as String }

    }
}

