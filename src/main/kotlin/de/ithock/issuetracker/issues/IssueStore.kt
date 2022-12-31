package de.ithock.issuetracker.issues

import com.google.gson.Gson
import com.google.gson.JsonSerializer
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.ActionCallback
import de.ithock.issuetracker.PluginBundle
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.settings.IssueTrackerAccount
import de.ithock.issuetracker.util.Helpers
import java.io.Serializable
import java.net.SocketTimeoutException

class IssueStore(@Volatile private var issues: MutableList<Issue>) : Iterable<Issue> {
    private var currentCallback: ActionCallback

    constructor() : this(mutableListOf())

    init {
        currentCallback = ActionCallback.DONE
    }

    fun update(repo: IssueTrackerAccount): ActionCallback {
        if (!isUpdating()) {
            currentCallback = ActionCallback()
            // Refresh Issue Task
        }
        return currentCallback
    }

    private fun isUpdating(): Boolean {
        return !currentCallback.isDone
    }

    fun getAllIssues(): List<Issue> {
        return issues
    }

    fun getIssue(index: Int): Issue {
        if (index < 0 || index >= issues.size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for issues with size ${issues.size}")
        }
        return issues[index]
    }

    fun getIndexOf(issue: Issue): Int {
        return issues.indexOf(issue)
    }

    fun getIssueById(id: String): Issue? {
        return issues.find { it.getIdentifier() == id }
    }

    override fun iterator(): Iterator<Issue> {
        return issues.iterator()
    }

    fun serialize(): String {
        // Use GSON to serialize all issues
        val serializer = JsonSerializer<Issue> { src, _, context ->
            val json = context.serialize(src)
            json.asJsonObject.addProperty("type", src.javaClass.name)
            json
        }
        val elements = issues.map {
            serializer.serialize(
                it,
                it.javaClass,
                null
            )
        }

        return Gson().toJson(elements)
    }

    companion object {
        fun deserialize(serialized: String): IssueStore {
            // Use GSON to deserialize all issues
            val elements = Gson().fromJson(serialized, Array<Issue>::class.java)
            return IssueStore(elements.toMutableList())
        }
    }

    class RefreshIssuesTask(
        private val future: ActionCallback,
        private val repo: IssueTrackerAccount,
        private val store: IssueStore
    ) : Task.Backgroundable(null, PluginBundle.get("task.refreshIssues"), true, ALWAYS_BACKGROUND) {

        override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
            try {
                Helpers.getLogger().debug("Fetching issues")
                // TODO: Fetch from API
                store.issues = mutableListOf()
            } catch (e: SocketTimeoutException) {
                displayErrorMessage("Failed to update issues from Server. Request timed out.", e)
            } catch (e: Exception) {
                displayErrorMessage("Can't connect to Server. Are you offline?", e)
            }
        }

        fun displayErrorMessage(message: String, e: Exception) {
            Helpers.getLogger().info("Issues refresh failed: ${e.message}")
            Helpers.getLogger().debug(e)
            setTitle(message)
            Thread.sleep(15000L)
        }

        override fun onCancel() {
            future.setDone()
            Helpers.getLogger().debug("Issue store refresh canncedl for Server: ${repo.url}")
        }

        override fun onSuccess() {
            future.setDone()
            Helpers.getLogger().debug("Issue store has been updated for Server: ${repo.url}")
            //ComponentAware.Companion.of(this.repo.getProject()).getIssueUpdaterComponent().onAfterUpdate();
        }
    }
}