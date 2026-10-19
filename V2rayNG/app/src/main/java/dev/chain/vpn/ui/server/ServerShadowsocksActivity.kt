package dev.chain.vpn.ui.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import dev.chain.vpn.R
import dev.chain.vpn.dto.entities.ProfileItem
import dev.chain.vpn.enums.EConfigType
import dev.chain.vpn.ui.compose.FormDropdownField
import dev.chain.vpn.ui.compose.FormTextField

class ServerShadowsocksActivity : BaseServerActivity() {

    override val serverConfigType: EConfigType = EConfigType.SHADOWSOCKS

    @Composable
    override fun ScreenContent() {
        val options = rememberFieldOptions()
        val scope = rememberCoroutineScope()
        val uiState = rememberSaveable(saver = ServerUiState.Saver) {
            ServerUiState.from(
                initialConfig = initialConfig
            )
        }.apply {
            configType = EConfigType.SHADOWSOCKS
        }
        val securityOptions = stringArrayResource(R.array.ss_securitys).toList()

        ServerEditorScaffold(
            title = serverConfigType.toString(),
            onSaveClick = { saveServer(uiState) }
        ) {
            CommonBasicFields(uiState)
            ShadowsocksProtocolFields(uiState, securityOptions)
            CommonNetworkFields(uiState, options)
            CommonStreamSecurityFields(
                state = uiState,
                options = options,
                scope = scope,
                buildProfileItem = { uiState.toProfileItem(initialConfig) }
            )
        }
    }

    override fun validateProtocolConfig(config: ProfileItem): Boolean = true

    @Composable
    private fun ShadowsocksProtocolFields(
        state: ServerUiState,
        methodOptions: List<String>
    ) {
        FormTextField(
            stringResource(R.string.server_lab_id3),
            state.password,
            { state.password = it }
        )
        FormDropdownField(
            stringResource(R.string.server_lab_security),
            state.method,
            methodOptions,
            { state.method = it }
        )
    }
}

