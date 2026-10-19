package dev.chain.vpn.ui.main.servers

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chain.vpn.R
import dev.chain.vpn.extension.isComplexType
import dev.chain.vpn.ui.main.FreeServersState
import dev.chain.vpn.ui.main.ImportMenuContent
import dev.chain.vpn.ui.main.MainAction
import dev.chain.vpn.ui.main.MainMoreMenuAction
import dev.chain.vpn.ui.main.MainViewModel
import dev.chain.vpn.ui.main.ServerRowUiModel
import dev.chain.vpn.ui.main.home.LatencyBadge
import dev.chain.vpn.ui.main.home.ProtocolChip
import dev.chain.vpn.ui.main.home.extractFlagEmoji
import dev.chain.vpn.ui.main.home.formatLastUpdated
import dev.chain.vpn.ui.main.home.latencyColor
import dev.chain.vpn.ui.main.serverMenuActions

@Composable
fun ChainServersScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val groupState by mainViewModel.serverGroupState(uiState.selectedGroupId)
        .collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }

    val pullState = rememberPullToRefreshState()
    val refreshing = uiState.freeServersState is FreeServersState.Working

    val removeServer: (String) -> Unit = { guid ->
        if (uiState.confirmRemove) showRemoveConfirm = guid
        else onAction(MainAction.RemoveServer(guid))
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showImportDialog = true },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(R.string.chain_servers_add)) },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            state = pullState,
            isRefreshing = refreshing,
            onRefresh = { onAction(MainAction.RefreshFreeServers) },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "free-servers") {
                    FreeServersCard(
                        freeState = uiState.freeServersState,
                        lastUpdated = uiState.freeServersLastUpdated,
                        onRefresh = { onAction(MainAction.RefreshFreeServers) },
                        onDismiss = { onAction(MainAction.DismissFreeServersState) },
                    )
                }

                item(key = "search-group") {
                    SearchAndGroupRow(
                        searchQuery = searchQuery,
                        onSearchChange = {
                            searchQuery = it
                            onAction(MainAction.Search(it))
                        },
                        groups = uiState.groups,
                        selectedGroupId = uiState.selectedGroupId,
                        onSelectGroup = { onAction(MainAction.SelectGroup(it)) },
                        onOverflowClick = { showOverflowMenu = true },
                    )
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                    ) {
                        MainMoreMenuAction.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(stringResource(entry.labelRes)) },
                                onClick = {
                                    showOverflowMenu = false
                                    onMoreMenuAction(entry)
                                },
                            )
                        }
                    }
                }

                if (isLoading && groupState.rows.isEmpty()) {
                    items(6, key = { "skeleton-$it" }) { SkeletonCard() }
                } else if (groupState.rows.isEmpty()) {
                    item(key = "empty") { EmptyState() }
                } else {
                    items(groupState.rows, key = { it.guid }) { row ->
                        ServerCard(
                            row = row,
                            selected = row.guid == uiState.selectedGuid,
                            onSelect = { onAction(MainAction.SelectServer(row.guid)) },
                            onAction = onAction,
                            onRemove = { removeServer(row.guid) },
                        )
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onAction = {
                showImportDialog = false
                onAction(it)
            },
        )
    }

    if (showRemoveConfirm != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRemoveConfirm = null },
            confirmButton = {
                TextButton(onClick = {
                    val guid = showRemoveConfirm
                    showRemoveConfirm = null
                    if (guid != null) onAction(MainAction.RemoveServer(guid))
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            text = { Text(stringResource(R.string.confirm_delete_profile)) },
        )
    }
}

@Composable
private fun FreeServersCard(
    freeState: FreeServersState,
    lastUpdated: Long,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.chain_free_servers),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = formatLastUpdated(lastUpdated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
                when (freeState) {
                    is FreeServersState.Working -> CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 3.dp,
                    )

                    else -> IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.chain_free_servers),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            when (freeState) {
                is FreeServersState.Working -> {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = freeState.statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                is FreeServersState.Success -> {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chain_free_success, freeState.count),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.chain_action_dismiss),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                is FreeServersState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = freeState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onRefresh) {
                            Text(stringResource(R.string.chain_action_retry))
                        }
                    }
                }

                FreeServersState.Idle -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndGroupRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    groups: List<dev.chain.vpn.dto.GroupMapItem>,
    selectedGroupId: String,
    onSelectGroup: (String) -> Unit,
    onOverflowClick: () -> Unit,
) {
    var groupMenuExpanded by remember { mutableStateOf(false) }
    val selectedRemarks = groups.firstOrNull { it.id == selectedGroupId }?.remarks
        ?: groups.firstOrNull()?.remarks.orEmpty()

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(stringResource(R.string.chain_servers_search_hint)) },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = groupMenuExpanded,
                onExpandedChange = { groupMenuExpanded = it },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = selectedRemarks,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.chain_servers_group_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupMenuExpanded)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = groupMenuExpanded,
                    onDismissRequest = { groupMenuExpanded = false },
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.remarks) },
                            onClick = {
                                groupMenuExpanded = false
                                onSelectGroup(group.id)
                            },
                        )
                    }
                }
            }
            IconButton(onClick = onOverflowClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.chain_action_more_options),
                )
            }
        }
    }
}

@Composable
private fun ServerCard(
    row: ServerRowUiModel,
    selected: Boolean,
    onSelect: () -> Unit,
    onAction: (MainAction) -> Unit,
    onRemove: () -> Unit,
) {
    val profile = row.profile
    var rowMenuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = extractFlagEmoji(row.remarks) ?: "\uD83C\uDF10",
                fontSize = 28.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.remarks.ifBlank { profile.server ?: "" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProtocolChip(profile.configType.name)
                    Spacer(Modifier.width(8.dp))
                    LatencyBadge(row.testDelayMillis)
                }
            }
            SignalBars(row.testDelayMillis)
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { rowMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.chain_action_more_options),
                    )
                }
                DropdownMenu(
                    expanded = rowMenuExpanded,
                    onDismissRequest = { rowMenuExpanded = false },
                ) {
                    serverMenuActions(
                        isComplexProfile = profile.configType.isComplexType(),
                        includeManagementActions = true,
                    ).forEach { menuAction ->
                        DropdownMenuItem(
                            text = { Text(stringResource(menuAction.labelRes)) },
                            onClick = {
                                rowMenuExpanded = false
                                when (menuAction) {
                                    dev.chain.vpn.ui.main.ServerMenuAction.ShareQRCode ->
                                        onAction(MainAction.ShareQRCode(row.guid))

                                    dev.chain.vpn.ui.main.ServerMenuAction.ShareClipboard ->
                                        onAction(MainAction.ShareClipboard(row.guid))

                                    dev.chain.vpn.ui.main.ServerMenuAction.ShareFullContent ->
                                        onAction(MainAction.ShareFullContent(row.guid))

                                    dev.chain.vpn.ui.main.ServerMenuAction.Edit ->
                                        onAction(MainAction.EditServer(row.guid, row.profile))

                                    dev.chain.vpn.ui.main.ServerMenuAction.Delete -> onRemove()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalBars(delayMs: Long) {
    val level = when {
        delayMs <= 0L -> 0
        delayMs < 300L -> 4
        delayMs < 800L -> 3
        delayMs < 1500L -> 2
        else -> 1
    }
    val color = latencyColor(delayMs)
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((7 + index * 5).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < level) color else color.copy(alpha = 0.22f)
                    )
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.chain_servers_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.chain_servers_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SkeletonCard() {
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun ImportDialog(
    onDismiss: () -> Unit,
    onAction: (MainAction) -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.chain_action_dismiss))
            }
        },
        title = { Text(stringResource(R.string.chain_servers_add)) },
        text = { ImportMenuContent(onAction = onAction) },
    )
}
