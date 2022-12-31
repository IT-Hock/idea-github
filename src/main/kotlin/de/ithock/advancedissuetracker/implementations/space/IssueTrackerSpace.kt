package de.ithock.advancedissuetracker.implementations.space

import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.implementations.IssueTracker
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.URLEncoder

class IssueTrackerSpace(override val connection: SpaceConnection) : IssueTracker(connection) {
    override fun createIssue(summary: String, description: String): Issue {
        TODO("Not yet implemented")
    }

    override fun getIssue(id: String): Issue? {
        TODO("Not yet implemented")
    }

    override fun getIssues(): List<IssueSpace> {
        return getIssues(IssueSorting.CREATED, false)
    }

    fun getIssues(
        sorting: IssueSorting,
        isDescending: Boolean,
        fullTextSearch: String? = null
    ): List<IssueSpace> {
        var query = ""
        if(fullTextSearch != null){
            query = "&query=${URLEncoder.encode(fullTextSearch, Charsets.UTF_8)}"
        }

        val descending = isDescending.toString()
        val createdBy = "createdBy(details(user(emails(email,blocked),avatar,id,name,smallAvatar,username)),name)"

        val assignee = "assignee(emails(email,blocked),avatar,id,name,smallAvatar,username)"
        val response = request(
            "projects/${connection.projectId}/planning/issues?sorting=$sorting&descending=$descending&\$fields=data(status(id,name,color,resolved),id,$assignee,$createdBy,creationTime,tags(name),description,title)$query"
        )
            ?: return emptyList()
        val json = response.body()?.string() ?: return emptyList()
        val jsonIssues = JSONObject(json).getJSONArray("data")
        return jsonIssues.map { IssueSpace(connection, it as JSONObject) }
    }

    override fun updateIssue(issue: Issue): Issue {
        TODO("Not yet implemented")
    }

    override fun deleteIssue(id: String): Boolean {
        TODO("Not yet implemented")
    }

    private fun processError(response: Response) {
        val error = response.body()?.string()
        println(error)
    }

    private fun request(url: String): Response? {
        val client = OkHttpClient()

        val authHeader = "Bearer ${connection.getAccessToken()}"
        val request = Request.Builder()
            .url("${connection.url}/api/http/$url")
            .header("Authorization", authHeader)
            .header("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            processError(response)
            return null
        }
        return response
    }

    enum class IssueSorting {
        UPDATED,
        CREATED,
        TITLE,
        DUE
    }
}