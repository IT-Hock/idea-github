package de.ithock.issuetracker

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.LocalChangeList

class IssueTrackerCommitMessageProvider : com.intellij.openapi.vcs.changes.ui.CommitMessageProvider {
    override fun getCommitMessage(forChangelist: LocalChangeList, project: Project): String? {
        return "This is a commit message provided by the Issue Tracker plugin."
    }
}