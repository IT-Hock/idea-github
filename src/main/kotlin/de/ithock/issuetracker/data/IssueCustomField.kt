package de.ithock.issuetracker.data

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class IssueCustomField(
    private val name: String,
    private val value: List<String>,
    private val foregroundColor: Color?,
    private val backgroundColor: Color?,
    private val isTextField: Boolean
){
    fun getFieldName(): String {
        return name
    }

    fun getFieldValues(): List<String> {
        return value
    }

    fun getForegroundColor(): Color? {
        return foregroundColor
    }

    fun getBackgroundColor(): Color? {
        return backgroundColor
    }

    fun isTextField(): Boolean {
        return isTextField
    }

    fun formatValues(): String {
        return value.map {
            formatValue(it)
        }.joinToString(", ")
    }

    private fun formatValue(value: String): String {
        var charSequence: CharSequence = value
        charSequence = SimpleDateFormat().format(Date(value.toLong()))
        return if (Regex("^[1-9][0-9]{12}").matches(charSequence)) charSequence as String else value
    }
}