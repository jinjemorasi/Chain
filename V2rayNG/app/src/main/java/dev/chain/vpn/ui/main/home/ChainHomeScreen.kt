package dev.chain.vpn.ui.main.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chain.vpn.R
import dev.chain.vpn.ui.compose.colorConnectedGlow
import dev.chain.vpn.ui.compose.colorConnectingGlow
import dev.chain.vpn.ui.compose.colorPing
import dev.chain.vpn.ui.compose.colorPingMid
import dev.chain.vpn.ui.compose.colorPingRed
import dev.chain.vpn.ui.compose.colorPingUntested
import dev.chain.vpn.ui.main.ConnectionStats
import dev.chain.vpn.ui.main.FreeServersState
import dev.chain.vpn.ui.main.MainAction
import dev.chain.vpn.ui.main.MainViewModel

private val FlagRegex = Regex("\uD83C[\uDDE6-\uDDFF]\uD83C[\uDDE6-\uDDFF]")

internal fun extractFlagEmoji(text: String): String? = FlagRegex.find(text)?.value

internal fun latencyColor(delayMs: Long): Color = when {
    delayMs <= 0L -> colorPingUntested
    delayMs < 300L -> colorPing
    delayMs < 800L -> colorPingMid
    else -> colorPingRed
}

@Composable
fun ChainHomeScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val stats by mainViewModel.connectionStats.collectAsStateWithLifecycle()
    val groupState by mainViewModel.serverGroupState(uiState.selectedGroupId)
        .collectAsStateWithLifecycle()
    val selected = groupState.servers.firstOrNull { it.guid == uiState.selectedGuid }

    val statusLabel = when {
        uiState.isTesting -> stringResource(R.string.chain_home_status_testing)
        uiState.isRunning -> stringResource(R.string.chain_home_status_connected)
        else -> stringResource(R.string.chain_home_status_disconnected)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusPill(
            text = statusLabel,
            connected = uiState.isRunning,
            testing = uiState.isTesting,
        )

        Spacer(Modifier.height(28.dp))

        ConnectButton(
            isRunning = uiState.isRunning,
            isTesting = uiState.isTesting,
            statusLabel = statusLabel,
            onClick = { onAction(MainAction.ToggleService) },
        )

        Spacer(Modifier.height(20.dp))

        CurrentServerCard(
            remarks = selected?.profile?.remarks,
            server = selected?.profile?.server,
            protocol = selected?.profile?.configType?.name,
            delayMs = selected?.testDelayMillis ?: 0L,
        )

        Spacer(Modifier.height(16.dp))

        if (uiState.isRunning && stats != null) {
            StatsCard(stats = stats!!)
            Spacer(Modifier.height(16.dp))
        }

        QuickActions(
            freeState = uiState.freeServersState,
            lastUpdated = uiState.freeServersLastUpdated,
            onRefreshFree = { onAction(MainAction.RefreshFreeServers) },
            onDismissFree = { onAction(MainAction.DismissFreeServersState) },
        )

        Spacer(Modifier.height(24.dp))
    }

    if (uiState.lastCrashReport != null) {
        CrashReportDialog(
            report = uiState.lastCrashReport!!,
            onCopyClose = {
                onAction(MainAction.CopyCrashReport)
                onAction(MainAction.DismissCrashReport)
            },
            onClose = { onAction(MainAction.DismissCrashReport) },
        )
    }
}

@Composable
private fun CrashReportDialog(
    report: String,
    onCopyClose: () -> Unit,
    onClose: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.chain_crash_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.chain_crash_intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = report,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCopyClose) {
                Text(stringResource(R.string.chain_crash_copy_close))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.chain_action_dismiss))
            }
        },
    )
}

@Composable
private fun StatusPill(text: String, connected: Boolean, testing: Boolean) {
    val color = when {
        connected -> colorConnectedGlow
        testing -> colorConnectingGlow
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@Composable
private fun ConnectButton(
    isRunning: Boolean,
    isTesting: Boolean,
    statusLabel: String,
    onClick: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "chain-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val active = isRunning || isTesting
    val glow = when {
        isRunning -> colorConnectedGlow
        isTesting -> colorConnectingGlow
        else -> Color.Transparent
    }
    val buttonColor = when {
        isRunning -> colorConnectedGlow
        isTesting -> colorConnectingGlow
        else -> MaterialTheme.colorScheme.secondary
    }
    val iconTint = when {
        isRunning || isTesting -> Color(0xFF06281C)
        else -> Color(0xFF221600)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp),
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(glow.copy(alpha = 0.16f))
            )
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .scale(2f - pulse)
                    .clip(CircleShape)
                    .background(glow.copy(alpha = 0.09f))
            )
        }
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = buttonColor,
            shadowElevation = if (active) 12.dp else 4.dp,
            modifier = Modifier.size(148.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Filled.Power,
                    contentDescription = statusLabel,
                    tint = iconTint,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun CurrentServerCard(
    remarks: String?,
    server: String?,
    protocol: String?,
    delayMs: Long,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            if (remarks.isNullOrBlank() && server.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.chain_home_no_server),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = extractFlagEmoji(remarks.orEmpty()) ?: "\uD83C\uDF10",
                        fontSize = 26.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = remarks?.ifBlank { server }.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!protocol.isNullOrBlank()) {
                                ProtocolChip(protocol)
                                Spacer(Modifier.width(8.dp))
                            }
                            LatencyBadge(delayMs)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProtocolChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
internal fun LatencyBadge(delayMs: Long) {
    val color = latencyColor(delayMs)
    val text = if (delayMs <= 0L) stringResource(R.string.chain_latency_untested)
    else "$delayMs ms"
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatsCard(stats: ConnectionStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatTile(
                    icon = Icons.Filled.Speed,
                    label = stringResource(R.string.chain_home_download),
                    value = formatSpeed(stats.downBps),
                )
                StatTile(
                    icon = Icons.Filled.Bolt,
                    label = stringResource(R.string.chain_home_upload),
                    value = formatSpeed(stats.upBps),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatTile(
                    icon = Icons.Filled.Timer,
                    label = stringResource(R.string.chain_home_duration),
                    value = formatDuration(stats.connectedSeconds),
                )
                StatTile(
                    icon = Icons.Filled.Public,
                    label = stringResource(R.string.chain_home_session_usage),
                    value = formatBytes(stats.sessionDownBytes + stats.sessionUpBytes),
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickActions(
    freeState: FreeServersState,
    lastUpdated: Long,
    onRefreshFree: () -> Unit,
    onDismissFree: () -> Unit,
) {
    FilledTonalButton(
        onClick = onRefreshFree,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(
                text = stringResource(R.string.chain_free_servers),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = formatLastUpdated(lastUpdated),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (freeState is FreeServersState.Working) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    when (freeState) {
        is FreeServersState.Working -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text(
                text = freeState.statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is FreeServersState.Success -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "✓ " + stringResource(R.string.chain_free_success, freeState.count),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorPing,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = onDismissFree, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.chain_action_dismiss),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        is FreeServersState.Error -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = freeState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(onClick = onRefreshFree) {
                Text(stringResource(R.string.chain_action_retry))
            }
        }

        FreeServersState.Idle -> {}
    }
}

@Composable
internal fun formatLastUpdated(millis: Long): String {
    if (millis <= 0L) return stringResource(R.string.chain_free_never_updated)
    val dateTime = java.text.DateFormat
        .getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
        .format(java.util.Date(millis))
    return stringResource(R.string.chain_free_last_updated, dateTime)
}

private fun formatSpeed(bytesPerSec: Long): String = when {
    bytesPerSec < 1024L -> "$bytesPerSec B/s"
    bytesPerSec < 1024L * 1024L -> "%.1f KB/s".format(bytesPerSec / 1024f)
    else -> "%.1f MB/s".format(bytesPerSec / 1048576f)
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "%.1f KB".format(bytes / 1024f)
    bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / 1048576f)
    else -> "%.2f GB".format(bytes / 1073741824f)
}

private fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
