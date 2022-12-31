package de.ithock.issuetracker.util

import de.ithock.issuetracker.PluginBundle

class SpaceApiException(private val apiException: SpaceApiExceptions) : Exception(apiException.getLocalizedMessage()) {
    constructor(errorKey: String, errorMessage: String = "") : this(SpaceApiExceptions.value(errorKey)) {
    }

    override fun getLocalizedMessage(): String {
        return PluginBundle.get(apiException.getLocalizedMessage())
    }

    enum class SpaceApiExceptions(private val key: String?, private val message: String) {
        PERMISSION_DENIED("permission-denied", "api.space.permission-denied"),
        UNKNOWN_ERROR(null, "api.space.unknown-error");

        fun getKey(): String? {
            return key
        }

        fun getLocalizedMessage(): String {
            return message
        }

        companion object {
            fun value(key: String): SpaceApiExceptions {
                return values().firstOrNull { it.key == key } ?: UNKNOWN_ERROR
            }
        }
    }
}