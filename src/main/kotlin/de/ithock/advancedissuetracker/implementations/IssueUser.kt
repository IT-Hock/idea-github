package de.ithock.advancedissuetracker.implementations
abstract class IssueUser(
) {
    var id:String? = null
    var firstName:String? = null
    var lastName:String? = null
    var avatar:String? = null
    var email:String? = null
    var username:String? = null

    /**
     * The URL to the profile of the user
     */
    var profileUrl: String? = null

    /**
     * Returns the full name of the user if available, otherwise the username
     */
    fun getFullName():String {
        return when {
            firstName != null && lastName != null -> {
                "$firstName $lastName"
            }
            else -> {
                username ?: ""
            }
        }
    }
}