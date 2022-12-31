package de.ithock.advancedissuetracker

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import de.ithock.advancedissuetracker.util.AvatarCache
import de.ithock.advancedissuetracker.util.IssueTrackerConnectionConverter

@Service(Service.Level.PROJECT)
@State(name = "AdvancedIssueTrackerService", category = SettingsCategory.PLUGINS,
    storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)])
class IssueTrackerService : PersistentStateComponent<IssueTrackerService> {
    @OptionTag(converter = IssueTrackerConnectionConverter::class)
    var connections: List<IssueTrackerConnection> = listOf()

    @com.intellij.util.xmlb.annotations.Transient
    var avatarCache: AvatarCache = AvatarCache()

    override fun getState(): IssueTrackerService {
        return this
    }

    override fun loadState(state: IssueTrackerService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): IssueTrackerService {
            return ApplicationManager.getApplication().getService(IssueTrackerService::class.java)
        }
    }
}