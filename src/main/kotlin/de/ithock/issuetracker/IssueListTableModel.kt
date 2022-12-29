package de.ithock.issuetracker

import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.data.IssueState
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHLabel
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.RowFilter

class IssueListRowFilter(val projectLabels: List<GHLabel>) : RowFilter<IssueListTableModel, Int>() {
    private var filterText: String? = null
    private var filterState: IssueState? = null
    private var filterLabels: MutableList<GHLabel> = ArrayList()
    private var filterAssignee: String? = null
    private var filterMilestone: String? = null
    private var filterAuthor: String? = null

    // Extract all filters from the given filter string
    private fun extractFilter() {
        // We allow filters to be separated by a comma, a semicolon, or a space
        val filterParts = filterText?.split(",|;|\\s".toRegex())?.toTypedArray()
        if (filterParts != null) {
            for (filterPart in filterParts) {
                // Get filter key
                val filterKey = filterPart.substringBefore(":").lowercase(Locale.getDefault())
                // Get filter value
                val filterValue = filterPart.substringAfter(":").lowercase(Locale.getDefault())

                // Check if the filter key is a valid filter key
                if (filterKey == "state" || filterKey == "is" || filterKey == "status") {
                    if (filterValue == "open") {
                        filterState = IssueState.OPEN
                    } else if (filterValue == "closed") {
                        filterState = IssueState.CLOSED
                    }
                } else if (filterKey == "label") {
                    // Pushback on filterLabels
                    val projectLabel = projectLabels.firstOrNull { it.name.lowercase(Locale.getDefault()) == filterValue }
                    if (projectLabel != null) {
                        filterLabels.add(projectLabel)
                    }
                } else if (filterKey == "assignee") {
                    filterAssignee = filterValue
                } else if (filterKey == "milestone") {
                    filterMilestone = filterValue
                } else if (filterKey == "author") {
                    filterAuthor = filterValue
                }
            }
        }
    }

    override fun include(entry: Entry<out IssueListTableModel, out Int>): Boolean {
        resetFilter()
        extractFilter()

        // Create a full-text search using a GitHub inspired syntax
        // https://docs.github.com/en/github/searching-for-information-on-github/getting-started-with-searching-on-github/understanding-the-search-syntax
        // is:open is:closed label:bug label:"help wanted" label:enhancement
        // summary:"issue summary" body:"issue body"
        // author:username mentions:username assignee:username
        // milestone:"milestone title" project:"project name"

        // If no filter is set, include all issues
        if (filterText == null && filterState == null && filterLabels.isEmpty() && filterAssignee == null && filterMilestone == null && filterAuthor == null) {
            return true
        }

        // Check if the issue state matches the filter
        if (filterState != null) {
            // Get Issue from the table model
            val issue = entry.model.getItem(entry.identifier)
            if (issue.getState() != filterState) {
                return false
            }
        }

        // Check if the issue labels match the filter
        if (filterLabels.isNotEmpty()) {
            // Get Issue from the table model
            val issue = entry.model.getItem(entry.identifier)
            if (!issue.getLabels().all { filterLabels.any { filterLabel -> filterLabel.name == it.getName() } }) {
                return false
            }
        }

        // Check if the issue assignee matches the filter
        /*if (filterAssignee != null) {
            // Get Issue from the table model
            val issue = entry.model.getItem(entry.identifier)
            if (issue.assignee?.login?.lowercase(Locale.getDefault()) != filterAssignee) {
                return false
            }
        }

        // Check if the issue milestone matches the filter
        if (filterMilestone != null) {
            // Get Issue from the table model
            val issue = entry.model.getItem(entry.identifier)
            if (issue.milestone?.title?.lowercase(Locale.getDefault()) != filterMilestone) {
                return false
            }
        }

        // Check if the issue author matches the filter
        if (filterAuthor != null) {
            // Get Issue from the table model
            val issue = entry.model.getItem(entry.identifier)
            if (issue.author?.login?.lowercase(Locale.getDefault()) != filterAuthor) {
                return false
            }
        }*/

        return true
    }

    private fun resetFilter() {
        filterState = null
        filterLabels = ArrayList()
        filterAssignee = null
        filterMilestone = null
        filterAuthor = null
    }

    fun setFilterText(filterText: String?) {
        this.filterText = filterText
    }
}

class IssueListTableModel : ListTableModel<Issue>() {
    private lateinit var unfilteredList: List<Issue>

    override fun setItems(items: MutableList<Issue>) {
        super.setItems(items)
        unfilteredList = listOf(*items.toTypedArray())
    }

    init {
        columnInfos = arrayOf(object : ColumnInfo<Issue, String>("Title") {
            override fun valueOf(item: Issue): String {
                if (item.getState() == IssueState.CLOSED) {
                    return "<html><s>${item.getTitle()}</s></html>"
                }

                return item.getTitle()
            }
        }, object : ColumnInfo<Issue, String>("Number") {
            override fun valueOf(item: Issue): String {
                return item.getIdentifier()
            }
        }, object : ColumnInfo<Issue, String>("State") {
            override fun valueOf(item: Issue): String {
                return item.getState().name
            }
        }, object : ColumnInfo<Issue, String>("Assignee") {
            override fun valueOf(item: Issue): String {
                if (item.getAssignee() != null) {
                    return item.getAssignee()!!
                }

                return ""
            }
        },

            object : ColumnInfo<Issue, String>("Milestone") {
                override fun valueOf(item: Issue): String {
                    if (item.getMilestone() != null) {
                        return item.getMilestone()!!
                    }

                    return ""
                }
            },

            object : ColumnInfo<Issue, String>("Created") {
                override fun valueOf(item: Issue): String {
                    // Format the date
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    return formatter.format(item.getCreatedAt().toInstant())
                }
            },

            object : ColumnInfo<Issue, String>("Updated") {
                override fun valueOf(item: Issue): String {
                    // Format the date
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    return ""
                }
            },

            object : ColumnInfo<Issue, String>("Closed") {
                override fun valueOf(item: Issue): String {
                    if (item.getClosedAt() != null) {
                        // Format the date
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        return formatter.format(item.getClosedAt()!!.toInstant())
                    }

                    return ""
                }
            })
    }
}