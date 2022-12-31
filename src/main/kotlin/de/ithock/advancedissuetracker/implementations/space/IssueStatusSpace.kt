package de.ithock.advancedissuetracker.implementations.space

import de.ithock.advancedissuetracker.implementations.IssueStatus
import org.json.JSONObject
import java.awt.Color

class IssueStatusSpace() : IssueStatus() {
    constructor(jsonStatus: JSONObject) : this() {
        identifier = jsonStatus.getString("id")
        name = jsonStatus.getString("name")
        resolved = jsonStatus.getBoolean("resolved")
        color = Color.decode("#${jsonStatus.getString("color")}")
    }
}