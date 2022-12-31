package de.ithock.advancedissuetracker.util

import com.intellij.util.io.HttpRequests
import de.ithock.issuetracker.util.Helpers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Image
import java.util.concurrent.CompletableFuture
import javax.swing.Icon
import javax.swing.ImageIcon

class AvatarCache {
    private val cache = mutableMapOf<String, CompletableFuture<Icon?>>()
    @OptIn(DelicateCoroutinesApi::class)
    fun load(url: String): CompletableFuture<Icon?> {
        return cache.getOrPut(url) {
            val iconFuture = CompletableFuture<Icon?>()
            GlobalScope.launch {
                try {
                    val bytes: ByteArray = HttpRequests.request(url).readBytes(null)
                    val tempIcon = ImageIcon(bytes)
                    val image: Image = tempIcon.image
                    val resizedImage: Image = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH)
                    iconFuture.complete(ImageIcon(resizedImage))
                } catch (e: Exception) {
                    Helpers.getLogger().error("Error while loading avatar", e)
                    iconFuture.complete(null)
                }
            }
            iconFuture
        }
    }

    fun get(url: String): CompletableFuture<Icon?>? {
        return cache[url]
    }
}