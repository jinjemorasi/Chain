package dev.chain.vpn.ui.main

import dev.chain.vpn.dto.ConnectionTestResult

sealed class MainServiceEvent {
    data object StateRunning : MainServiceEvent()
    data object StateNotRunning : MainServiceEvent()
    data object StateStartSuccess : MainServiceEvent()
    data object StateStartFailure : MainServiceEvent()
    data object StateStopSuccess : MainServiceEvent()
    data class MeasureDelayResult(val result: ConnectionTestResult) : MainServiceEvent()
    data class MeasureConfigSuccess(val guid: String) : MainServiceEvent()
    data class MeasureConfigNotify(val progress: String) : MainServiceEvent()
    data class MeasureConfigFinish(val finishedCount: String?) : MainServiceEvent()
}
