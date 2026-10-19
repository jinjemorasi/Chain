package dev.chain.vpn.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chain.vpn.R
import dev.chain.vpn.ui.compose.QRCodeDialog
import dev.chain.vpn.ui.main.home.ChainHomeScreen
import dev.chain.vpn.ui.main.servers.ChainServersScreen
import dev.chain.vpn.ui.main.settings.ChainSettingsScreen

private enum class ChainTab(val icon: ImageVector, val labelRes: Int) {
    HOME(Icons.Filled.Home, R.string.chain_tab_home),
    SERVERS(Icons.Filled.Dns, R.string.chain_tab_servers),
    SETTINGS(Icons.Filled.Settings, R.string.chain_tab_settings),
}

/**
 * Chain's redesigned main screen: bottom navigation across Home (dashboard),
 * Servers (modern server list) and Settings (feature hub). All backend logic
 * lives in [MainViewModel]; this file is presentation only.
 */
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    var tab by rememberSaveable { mutableStateOf(ChainTab.HOME) }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }

    val onMoreMenuAction: (MainMoreMenuAction) -> Unit = { menuAction ->
        when (menuAction) {
            MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
            MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
            MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
            MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
            MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
            MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
            MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
            MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
            MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
            MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
        }
    }

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareQRCodeBitmap != null) {
        QRCodeDialog(
            bitmap = shareQRCodeBitmap,
            onDismiss = { onAction(MainAction.DismissQRCodeDialog) }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ChainTab.entries.forEach { chainTab ->
                    NavigationBarItem(
                        selected = tab == chainTab,
                        onClick = { tab = chainTab },
                        icon = {
                            Icon(
                                imageVector = chainTab.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(chainTab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith
                        fadeOut(animationSpec = tween(220))
            },
            label = "chain-tab-transition",
            modifier = Modifier.padding(innerPadding),
        ) { currentTab ->
            when (currentTab) {
                ChainTab.HOME -> ChainHomeScreen(
                    mainViewModel = mainViewModel,
                    onAction = onAction,
                )

                ChainTab.SERVERS -> ChainServersScreen(
                    mainViewModel = mainViewModel,
                    onAction = onAction,
                    onMoreMenuAction = onMoreMenuAction,
                )

                ChainTab.SETTINGS -> ChainSettingsScreen(
                    onNavigate = onNavigate,
                )
            }
        }
    }
}
