package de.ithock.issuetracker.util

import com.github.javafaker.Color
import de.ithock.issuetracker.data.IssueComment
import java.util.*
import com.github.javafaker.Faker
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.data.IssueLabel
import de.ithock.issuetracker.data.IssueState

class FakeIssueDataProvider {
    companion object {

        private val random = Random()
        private val faker = Faker()
        fun getFakeDate(): Date {
            return Date(Date().time - random.nextInt(1000000000))
        }

        fun getFakeLabel(): IssueLabel {
            val color = java.awt.Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
            return IssueLabel(
                faker.idNumber().valid(),
                faker.lorem().word(),
                "#${color.red.toString(16)}${color.green.toString(16)}${color.blue.toString(16)}"
            )
        }

        fun getFakeLabels(
            amount: Int = random.nextInt(5),
        ): List<IssueLabel> {
            val labels = mutableListOf<IssueLabel>()
            for (i in 0..amount) {
                labels.add(getFakeLabel())
            }
            return labels
        }

        fun getFakeIssue(): Issue {
            val issueState = if (random.nextBoolean()) IssueState.OPEN else IssueState.CLOSED
            val closedDate = if (issueState == IssueState.CLOSED) getFakeDate() else null
            val milestone = if (random.nextBoolean()) faker.app().name() else null
            return Issue(
                // Random UUID
                faker.idNumber().valid(),
                faker.book().title(),
                faker.lorem().paragraph(),
                issueState,
                faker.name().fullName(),
                faker.name().fullName(),
                getFakeDate(),
                closedDate,
                getFakeLabels(),
                milestone,
                getFakeComments(),
                faker.internet().url()
            )
        }

        fun getFakeIssues(
            amount: Int = random.nextInt(100),
        ): List<Issue> {
            val issues = mutableListOf<Issue>()
            for (i in 0..amount) {
                issues.add(getFakeIssue())
            }
            return issues
        }

        fun getFakeComment(): IssueComment {
            return IssueComment(
                faker.idNumber().valid(),
                faker.lorem().paragraph(),
                faker.name().fullName(),
                getFakeDate(),
                faker.internet().url()
            )
        }

        fun getFakeComments(
            amount: Int = random.nextInt(100)
        ): List<IssueComment> {
            // Use JavaFaker to generate fake data
            val comments = mutableListOf<IssueComment>()
            for (i in 0..amount) {
                comments.add(getFakeComment())
            }
            return comments
        }
    }
}