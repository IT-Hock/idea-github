package de.ithock.advancedissuetracker.activities

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectPostStartupActivity
import de.ithock.advancedissuetracker.IssueTrackerProjectService
import de.ithock.advancedissuetracker.implementations.space.SpaceConnection
import de.ithock.advancedissuetracker.implementations.space.IssueTrackerSpace
import java.util.UUID

@Suppress("UnstableApiUsage")
class PluginProjectPostStartupActivity : ProjectPostStartupActivity {
    override suspend fun execute(project: Project) {
        val service = IssueTrackerProjectService.getInstance(project)
        if (service.issues.isNotEmpty()) {
            return
        }

        // For now, just fetch issues here :)
        val conn = SpaceConnection(
            "https://space.subtixx.de",
            UUID.randomUUID().toString(),
            "key:TEST",
            Credentials(
                "",
                "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIwNWZhNWYzNC04ZDRjLTQxZmUtYTRkYy0xMGEyOTRkZmE1MTMiLCJhdWQiOiIwNWZhNWYzNC04ZDRjLTQxZmUtYTRkYy0xMGEyOTRkZmE1MTMiLCJvcmdEb21haW4iOiJpdGhvY2siLCJuYW1lIjoiVGVzdCBBcHBsaWNhdGlvbiIsImlzcyI6Imh0dHBzOlwvXC9zcGFjZS5zdWJ0aXh4LmRlIiwicGVybV90b2tlbiI6IjIwVkZ3NzA2aWhITCIsInByaW5jaXBhbF90eXBlIjoiU0VSVklDRSIsImlhdCI6MTY3MjQwMDE3Mn0.p6JIBjZPK2FUStUFgOxbGyAZlfRaJRRrtKYFmMVjpF9SenlEQfjQR3QnsnBy4G-E2nL-OuizUWnD65EJZU1h_9xEsMIf3-ROOWa4mk8HUI_VQFyJDkWnDfg_HqOgtaRja2JDeq8wmXqNd6x8Mc_tjaDf0b73wosU4Eve83y_JBRVt81J5ill4U5z1A07uUg2-7fpdEmqpEsL9pNcWqCi8gYWgwC1joM2iNkqhORaXo-WnODHQLSIP_jzEfBtv6gekvKNyrSpNdRopaW4M0i-8_gXNdOljjcLS187IPG_aCzJNn8K8Hyh6uGyOpPArvBZq4O7v79YTGLNfsQ4o5z1vKlMOga869wgErFSD5b3u8i3TyqiBlyVcHbyS_JelFIS9H9FEYC3xLuu7zkIDYIRs6DTqq5g20HUI9Gvbsm2hEmDTtr4fCivTKaXRcmWYIFGiFSBWUUNfkZ_LQ0pCTd3zwqYShX7v_a-3KmDktjfSaGz7tg1HV-t31IRHcv-UPye8a-rHaxNesWWRDUuod7-ToQWLgZ8MPq3-2xB1QzuM8nocvZgUhW-n06QNixCsHreLMqkeIqRqy2z89ioesPEh028U9xHTrSbiFp0GxbKITN-CL1sFV7xT1ZRZCE2WtmpyIFk2rAHUpw1ozub0kxyJyxoisWYap0GcVfBtYe7ZYU"
            )
        )
        service.projectId = "key:TEST"
        service.connection = conn
        val issueTrackerSpace = IssueTrackerSpace(conn)
        val issues = issueTrackerSpace.getIssues()
        println("Found ${issues.size} issues")
        service.issues = issues
    }
}