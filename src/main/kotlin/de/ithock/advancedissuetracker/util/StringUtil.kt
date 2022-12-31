package de.ithock.advancedissuetracker.util

class StringUtil {
    companion object {
        fun nl2br(str: String): String {
            return str.replace(System.lineSeparator(), "<br>")
        }

        fun br2nl(str: String): String {
            return str.replace("<br/>", System.lineSeparator())
        }
    }
}