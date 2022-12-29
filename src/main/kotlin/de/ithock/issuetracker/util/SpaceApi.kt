package de.ithock.issuetracker.util

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.rd.util.toPromise
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.data.IssueLabel
import de.ithock.issuetracker.data.IssueState
import kotlinx.coroutines.*
import okhttp3.*
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.rejectedPromise
import org.jetbrains.concurrency.resolvedPromise
import org.json.JSONObject
import java.io.IOException
import java.net.InetSocketAddress
import java.util.*
import javax.swing.SwingUtilities


class SpaceApi(private val spaceConnection: SpaceConnection) : Callback, HttpHandler {
    private val client = OkHttpClient()
    private var httpServer: HttpServer? = null

    init {
        // Check if we have a valid bearer token
        if (spaceConnection.accessToken != null && spaceConnection.accessTokenExpire != null) {
            // Check if the token is still valid
            if (spaceConnection.accessTokenExpire!!.before(Date())) {
                // Token is expired, so we need to get a new one
                TODO("Get new token")
            }
        } else {
            authorizeUser()
        }
    }

    /**
     * Authorize the user by opening a web browser inside IntelliJ
     */
    private fun authorizeUser() {
        val scopes : List<String> = listOf(
            "global:Project.Issues.Create",
            "global:Project.Issues.View",
            "global:Project.Issues.Edit",
            "global:Project.Issues.Delete",
            "global:Project.Issues.Import",
            "global:Project.Issues.Restore",
            "global:Project.Issues.Manage",
            "global:Project.Documents.Edit",
            "global:Project.Documents.View",
        )
        val scope = scopes.joinToString(",")
        val redirectUri = "http://localhost:5252/space"
        httpServer = HttpServer.create(InetSocketAddress(8000), 0)
        httpServer!!.createContext("/space", this)
        httpServer!!.executor = null
        httpServer!!.start()
        BrowserUtil.browse("${spaceConnection.spaceUrl}/oauth/auth?response_type=code&state=aitidea&redirect_uri=$redirectUri&request_credentials=offline&client_id=${spaceConnection.clientId}&scope=${scope}&access_type=offline")
    }

    fun fetchIssues(spaceApiUrl: String, spaceApiToken: String): List<Issue> {
        TODO("Not yet implemented")
        return listOf()
    }

    private fun parseResponse(responseBody: String?): List<Issue> {
        val issues = mutableListOf<Issue>()

        if (responseBody != null) {
            val jsonResponse = JSONObject(responseBody)
            val jsonIssues = jsonResponse.getJSONArray("items")

            for (i in 0 until jsonIssues.length()) {
                val jsonIssue = jsonIssues.getJSONObject(i)

                // Answer looks like this
                /*{
                      "id": "1bGvBu0cSJ4s",
                      "createdBy": {
                        "name": "subtixx"
                      },
                      "creationTime": {
                        "iso": "2022-12-27T12:25:34.422Z",
                        "timestamp": 1672143934422
                      },
                      "assignee": null,
                      "status": {
                        "name": "Open",
                        "resolved": false,
                        "color": "1965b0"
                      },
                      "tags": [],
                      "title": "This is a test issue",
                      "description": "Hello this is a test issue to have some examples to test something with issues"
                    }
                 */
                val id = jsonIssue.getString("id")
                val title = jsonIssue.getString("title")
                val description = jsonIssue.getString("description")
                val status = jsonIssue.getJSONObject("status").getString("name")
                val statusColor = jsonIssue.getJSONObject("status").getString("color")
                val statusResolved = jsonIssue.getJSONObject("status").getBoolean("resolved")
                val author = jsonIssue.getJSONObject("createdBy").getString("name")
                val assignee = jsonIssue.getJSONObject("assignee").getString("name")
                val creationTime = jsonIssue.getJSONObject("creationTime").getNumber("timestamp")
                val dueDate = jsonIssue.getJSONObject("dueDate").getNumber("timestamp")
                val tags = jsonIssue.getJSONArray("tags").toList()

                /*
                Issue Requires:
                        this.identifier = identifier

                        this.title = title
                        this.body = body

                        this.state = state
                        this.milestone = milestone

                        this.author = author
                        this.assignee = assignee
                        this.createdAt = createdAt
                        this.closedAt = closedAt
                        this.labels = labels
                */
                val issueState: IssueState = when (status) {
                    "Open" -> IssueState.OPEN
                    "Closed" -> IssueState.CLOSED
                    else -> IssueState.OPEN
                }
                issues.add(
                    Issue(
                        id,
                        title,
                        description,
                        issueState,
                        author,
                        assignee,
                        // Create Date from timestamp
                        Date(creationTime.toLong()),
                        null,
                        tags as List<IssueLabel>,
                        null,
                        emptyList(),
                        "",
                    )
                )
            }
        }

        return issues
    }

    companion object {
        @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
        fun checkConnection(url: String): Promise<Boolean> {
            // Create async promise
            val promise = GlobalScope.async {
                try {

                    val client = OkHttpClient()
                    val request = Request.Builder().url("$url/api/http").build()

                    val response = client.newCall(request).execute()
                    if (response.code() != 401) {
                        response.close()
                        error("Invalid response code: ${response.code()}")
                    }
                    response.close()
                    return@async true
                } catch (e: IOException) {
                    error("Failed to connect to Space API")
                }
            }
            return promise.toPromise()
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        // Check which call failed
        if (call.request().url().toString().contains("oauth/token")) {
            // Failed to get bearer token
            error("Failed to get bearer token")
        } else {
            // Failed to get issues
            error("Failed to get issues")
        }
    }

    override fun onResponse(call: Call, response: Response) {
        // Check which call failed
        if (call.request().url().toString().contains("oauth/token")) {
            // Failed to get bearer token
            error("Failed to get bearer token")
        } else {
            // Failed to get issues
            error("Failed to get issues")
        }
    }

    override fun handle(exchange: HttpExchange?) {
        if (httpServer == null || exchange == null || exchange.requestURI == null || exchange.requestURI.query == null) {
            return
        }
        exchange.sendResponseHeaders(200, 0)
        // Parse Query into a map
        val query = exchange.requestURI.query.split("&").map { it.split("=") }.associate { it[0] to it[1] }
        if (query.contains("error")) {
            stopWebServer(exchange, "Error: ${query["error"]}")
            return
        }

        if (!query.contains("state") || !query.contains("code")) {
            stopWebServer(exchange, "Error: Invalid query")
            return
        }
        val state = query.getValue("state")
        val code = query.getValue("code")

        // Check if the state is valid
        if (state != "aitidea") {
            stopWebServer(exchange, "Error: Invalid state")
            return
        }

        stopWebServer(exchange)

        // Get the bearer token
        finalizeTokenExchange(code).onSuccess {
            SwingUtilities.invokeLater {
                onTokenExchangeSuccess()
            }
        }.onError {
            SwingUtilities.invokeLater {
                onTokenExchangeError()
            }
        }
    }

    private fun onTokenExchangeError() {
        TODO("Not yet implemented")
    }

    private fun onTokenExchangeSuccess() {
        TODO("Not yet implemented")
    }

    private fun finalizeTokenExchange(authorizationCode: String): Promise<Boolean> {
        val authorizationHeader = "Basic ${
            Base64.getEncoder()
                .encodeToString("${spaceConnection.clientId}:${spaceConnection.clientSecret}".toByteArray())
        }"
        val client = OkHttpClient()
        val request =
            Request.Builder().url("${spaceConnection.spaceUrl}/oauth/token").header("Accept", "application/json")
                .header("Authorization", "Basic $authorizationHeader")
                .header("Content-Type", "application/x-www-form-urlencoded").post(
                    FormBody.Builder().add("grant_type", "authorization_code").add("code", authorizationCode)
                        .add("redirect_uri", "http://localhost:8080")
                        // TODO: Implement PKCE
                        //.add("code_verifier", codeVerifier)
                        .build()
                )

        val response = client.newCall(request.build()).execute()
        if (response.code() != 200) {
            rejectedPromise<String>("Failed to access token: ${response.code()}")
        }

        val responseBody = response.body()
        if (responseBody == null) {
            rejectedPromise<String>("Failed to access token: No response body")
        }

        val json = JSONObject(responseBody!!.string())
        if (!json.has("access_token")) {
            rejectedPromise<String>("Failed to access token: No access token")
        }

        val accessToken = json.getString("access_token")

        if (!json.has("expires_in")) {
            rejectedPromise<String>("Failed to access token: No expire date?")
        }
        val expiresIn = json.getNumber("expires_in")

        // Save the token
        spaceConnection.accessToken = accessToken
        spaceConnection.accessTokenExpire = Date(Date().time + expiresIn.toLong())
        response.close()

        return resolvedPromise(true)
    }

    private fun stopWebServer(exchange: HttpExchange, response: String? = null) {
        if (httpServer == null) {
            return
        }

        if (response != null) {
            exchange.responseBody.write(response.toByteArray())
        }
        exchange.responseBody.close()

        httpServer!!.stop(0)
        httpServer = null
    }
}