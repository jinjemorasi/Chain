package dev.chain.vpn.ui.main

import dev.chain.vpn.dto.ConnectionTestResult
import dev.chain.vpn.dto.GroupMapItem
import dev.chain.vpn.dto.LocateTarget

/** Locale-neutral state formatted only when it reaches the main UI. */
sealed interface MainStatus {
    data object Disconnected : MainStatus
    data object Connected : MainStatus
    data object Testing : MainStatus
    data class TestProgress(val progress: String) : MainStatus
    data class ConnectionTest(val result: ConnectionTestResult) : MainStatus
}

/**
 * Main UI state
 */
data class MainUiState(
    val groups: List<GroupMapItem> = emptyList(),
    val selectedGroupId: String = "",
    val selectedGuid: String? = null,
    val isRunning: Boolean = false,
    val isTesting: Boolean = false,
    val status: MainStatus = MainStatus.Disconnected,
    val locateTarget: LocateTarget? = null,
    val confirmRemove: Boolean = false,
    val doubleColumnDisplay: Boolean = false,
    val shareQRCodeBitmap: android.graphics.Bitmap? = null,
    val freeServersState: FreeServersState = FreeServersState.Idle,
    val freeServersLastUpdated: Long = -1L,
    /** Crash report captured by [dev.chain.vpn.util.CrashReporter], shown once. */
    val lastCrashReport: String? = null
)

/** State of Chain's one-tap free-servers refresh (source id "chain-free"). */
sealed interface FreeServersState {
    data object Idle : FreeServersState
    data class Working(val statusText: String) : FreeServersState
    data class Success(val count: Int) : FreeServersState
    data class Error(val message: String) : FreeServersState
}

/** Live connection statistics polled from the core while the VPN is running. */
data class ConnectionStats(
    val upBps: Long = 0L,
    val downBps: Long = 0L,
    val connectedSeconds: Long = 0L,
    val sessionUpBytes: Long = 0L,
    val sessionDownBytes: Long = 0L
)

/**
 * All possible user interaction intents
 */
sealed interface MainAction {
    data object Initialize : MainAction
    data object RefreshGroups : MainAction
    data object ToggleService : MainAction
    data object TestCurrentServer : MainAction
    data object TestAllServers : MainAction
    data object TestRealAllServers : MainAction
    data object CancelTesting : MainAction
    data object RemoveAllServers : MainAction
    data object RemoveDuplicateServers : MainAction
    data object RemoveInvalidServers : MainAction
    data object SortByTestResults : MainAction
    data object UpdateSubscriptions : MainAction
    data object ExportAll : MainAction

    data object ImportQRcode : MainAction
    data object ImportClipboard : MainAction
    data object ImportConfigLocal : MainAction
    data class ImportManually(val type: Int) : MainAction
    data object RestartService : MainAction
    data object LocateSelectedServer : MainAction

    data class SelectGroup(val groupId: String) : MainAction
    data class SelectServer(val guid: String) : MainAction
    data class RemoveServer(val guid: String) : MainAction
    data class EditServer(val guid: String, val profile: dev.chain.vpn.dto.entities.ProfileItem) : MainAction
    data class Search(val query: String) : MainAction
    data class ShareQRCode(val guid: String) : MainAction
    data class ShareClipboard(val guid: String) : MainAction
    data class ShareFullContent(val guid: String) : MainAction
    data object DismissQRCodeDialog : MainAction

    data class ImportBatchConfig(val configText: String) : MainAction

    data object LocateHandled : MainAction

    // ---- Chain additions ----
    data object RefreshFreeServers : MainAction
    data object DismissFreeServersState : MainAction
    data object DismissCrashReport : MainAction
    data object CopyCrashReport : MainAction
}
