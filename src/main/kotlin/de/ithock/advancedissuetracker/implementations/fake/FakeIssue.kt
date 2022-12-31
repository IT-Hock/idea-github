package de.ithock.advancedissuetracker.implementations.fake

import com.github.javafaker.Faker
import com.intellij.ui.JBColor
import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.implementations.IssueStatus
import java.util.*
import java.util.concurrent.TimeUnit

class FakeIssue() : Issue() {
    init {
        identifier = Faker().number().digits(5)
        summary = Faker().lorem().sentence()
        body = Faker().lorem().paragraphs(5).joinToString("\r")
        state = generateStatus()
        milestone = "1.0"
        author = FakeIssueUser()
        assignee = generateAssignee()
        createdAt = Faker().date().past(100, TimeUnit.DAYS)
        closedAt = generateClosedAtDate()
        labels = emptyList()
        comments = emptyList()
        url = "https://it-hock.de"
    }

    private fun generateClosedAtDate(): Date? {
        return when {
            state.resolved -> {
                Faker().date().between(createdAt, Date())
            }

            else -> null
        }
    }

    private fun generateAssignee(): FakeIssueUser? {
        return when {
            Faker.instance().bool().bool() -> {
                FakeIssueUser()
            }

            else -> null
        }
    }

    private fun generateStatus() : IssueStatus {
        val isResolved = Faker.instance().bool()?.bool() ?: false
        return IssueStatus(
            Faker().number().digits(5),
            Faker().lorem().word(),
            isResolved,
            if (isResolved) JBColor.GREEN else JBColor.RED
        )
    }
}