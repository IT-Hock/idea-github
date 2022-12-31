package de.ithock.issuetracker.issues

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectPostStartupActivity
import de.ithock.issuetracker.util.Helpers
import java.util.concurrent.TimeUnit.*
import javax.swing.SwingUtilities

@Suppress("UnstableApiUsage")
class IssuesUpdaterInitExtension : ProjectPostStartupActivity {
    override suspend fun execute(project: Project) {
        // Start IssueStoreUpdaterTask
        IssueStoreUpdaterTask(project).queue()
    }
}