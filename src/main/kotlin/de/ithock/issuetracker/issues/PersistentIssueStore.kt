package de.ithock.issuetracker.issues

import com.intellij.openapi.components.PersistentStateComponent
import de.ithock.issuetracker.settings.IssueTrackerAccount
import java.util.concurrent.ConcurrentHashMap

class PersistentIssueStore : PersistentStateComponent<PersistentIssueStore.Memento> {
    private var loadedMemento = Memento()

    private val stores: ConcurrentHashMap<String, IssueStore> = ConcurrentHashMap()

    override fun getState(): Memento {
        return Memento(stores)
    }

    override fun loadState(state: Memento) {
        loadedMemento = state
    }

    fun get(repo: IssueTrackerAccount): IssueStore {
        val issueStore = stores[repo.uniqueId]
        if (issueStore == null) {
            loadedMemento.getStore(repo)?.let {
                stores[repo.uniqueId] = it
            }
        }
        return stores[repo.uniqueId] ?: IssueStore()
    }

    fun remove(repo: IssueTrackerAccount) {
        stores.remove(repo.uniqueId)
    }

    class Memento() {
        private val persistentIssues: LinkedHashMap<String, String> = LinkedHashMap()

        constructor(stores: Map<String, IssueStore>) : this() {
            // Serialize all stores
            stores.forEach { (key, value) ->
                persistentIssues[key] = value.serialize()
            }
        }

        fun setPersistentIssues(set: Map<String, String>) {
            persistentIssues.clear()
            persistentIssues.putAll(set)
        }

        fun getStore(repo: IssueTrackerAccount): IssueStore? {
            val serialized = persistentIssues[repo.uniqueId]
            if (serialized != null) {
                return IssueStore.deserialize(serialized)
            }
            return null
        }
    }
}