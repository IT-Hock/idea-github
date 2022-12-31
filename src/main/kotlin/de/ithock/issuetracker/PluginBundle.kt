package de.ithock.issuetracker

import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier


class PluginBundle() : DynamicPluginBundle("messages.main") {
    operator fun get(
        key: String,
        vararg params: Any
    ): String {
        return INSTANCE.getMessage(key, *params)
    }

    fun lazy(
        key: String,
        vararg params: Any
    ): Supplier<String> {
        return INSTANCE.getLazyMessage(key, *params)
    }

    companion object{
        val INSTANCE: PluginBundle = PluginBundle()
        fun getInstance(): PluginBundle {
            return INSTANCE
        }

        fun get(
            @PropertyKey(resourceBundle = "messages.main") key: String,
            vararg params: Any
        ): String {
            return INSTANCE.getMessage(key, *params)
        }

        fun lazyMessage(
            @PropertyKey(resourceBundle = "messages.main") key: String,
            vararg params: Any
        ): Supplier<String> {
            return INSTANCE.getLazyMessage(key, *params)
        }
    }
}