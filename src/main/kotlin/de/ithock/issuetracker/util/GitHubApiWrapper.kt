package de.ithock.issuetracker.util

import de.ithock.issuetracker.data.GitHubProject
import de.ithock.issuetracker.data.Issue
import org.kohsuke.github.GHAuthorization
import org.kohsuke.github.GitHub

class GitHubApiWrapper(accessToken: String?) {
    private var github: GitHub

    init {
        github = if (accessToken != null) {
            GitHub.connectUsingOAuth(accessToken)
        } else {
            GitHub.connectAnonymously()
        }
    }

    fun getAccessToken(): String {
        val githubAuth: GHAuthorization = github.createOrGetAuth(
            GlobalConstants.GITHUB_CLIENT_ID,
            GlobalConstants.GITHUB_CLIENT_SECRET,
            listOf("repo", "org:read"),
            "Advanced Issue Tracker",
            "https://it-hock.de"
        )
        github = GitHub.connectUsingOAuth(githubAuth.token)
        return githubAuth.token
    }

    fun getProjects(): List<GitHubProject> {
        val projects = mutableListOf<GitHubProject>()
        github.myself.allOrganizations.forEach { org ->
            org.repositories.forEach { repo ->
                projects.add(GitHubProject(repo.value.name, repo.value.fullName, repo.value.htmlUrl.toString()))
            }
        }
        github.myself.allRepositories.forEach { repo ->
            projects.add(GitHubProject(repo.value.name, repo.value.fullName, repo.value.htmlUrl.toString()))
        }
        return projects
    }

    fun getIssueList(project: GitHubProject): List<Issue> {
        val issues = mutableListOf<Issue>()
        val repo = github.getRepository(project.fullName)
        repo.getIssues(null).forEach { issue ->
            issues.add(Issue(issue))
        }
        return issues
    }

    fun getIssue(project: GitHubProject, issueNumber: Int): Issue {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        return Issue(issue)
    }

    fun createIssue(project: GitHubProject, title: String, body: String): Issue {
        val repo = github.getRepository(project.fullName)
        val issue = repo.createIssue(title)
        issue.body(body)
        val createdIssue = issue.create()
        return Issue(createdIssue)
    }

    fun updateIssue(project: GitHubProject, issueNumber: Int, title: String, body: String): Issue {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.title = title
        issue.body = body
        return Issue(issue)
    }

    fun deleteIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.close()
        issue.lock()
    }

    fun getIssueComments(project: GitHubProject, issueNumber: Int): List<String> {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        val comments = mutableListOf<String>()
        issue.comments.forEach { comment ->
            comments.add(comment.body)
        }
        return comments
    }

    fun addIssueComment(project: GitHubProject, issueNumber: Int, comment: String) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.comment(comment)
    }

    fun closeIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.close()
    }

    fun reopenIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.reopen()
    }

    fun lockIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.lock()
    }

    fun unlockIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.unlock()
    }

    fun assignIssue(project: GitHubProject, issueNumber: Int, assignee: String) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        github.getUser(assignee).let { user ->
            issue.assignTo(user)
        }
    }

    fun unassignIssue(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.assignTo(null)
    }

    fun addLabel(project: GitHubProject, issueNumber: Int, label: String) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.addLabels(label)
    }

    fun removeLabel(project: GitHubProject, issueNumber: Int, label: String) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.removeLabels(label)
    }

    fun addMilestone(project: GitHubProject, issueNumber: Int, milestone: String) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        repo.listMilestones(null).forEach { ms ->
            if (ms.title == milestone) {
                issue.milestone = ms
            }
        }
    }

    fun removeMilestone(project: GitHubProject, issueNumber: Int) {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        issue.milestone = null
    }

    fun getMilestones(project: GitHubProject): List<String> {
        val repo = github.getRepository(project.fullName)
        val milestones = mutableListOf<String>()
        repo.listMilestones(null).forEach { ms ->
            milestones.add(ms.title)
        }
        return milestones
    }

    fun getLabels(project: GitHubProject): List<String> {
        val repo = github.getRepository(project.fullName)
        val labels = mutableListOf<String>()
        repo.listLabels().forEach { label ->
            labels.add(label.name)
        }
        return labels
    }

    fun getAssignees(project: GitHubProject): List<String> {
        val repo = github.getRepository(project.fullName)
        val assignees = mutableListOf<String>()
        repo.listAssignees().forEach { assignee ->
            assignees.add(assignee.login)
        }
        return assignees
    }

    fun getIssueEvents(project: GitHubProject, issueNumber: Int): List<String> {
        val repo = github.getRepository(project.fullName)
        val issue = repo.getIssue(issueNumber)
        val events = mutableListOf<String>()
        issue.listEvents().forEach { event ->
            events.add(event.event)
        }
        return events
    }
}