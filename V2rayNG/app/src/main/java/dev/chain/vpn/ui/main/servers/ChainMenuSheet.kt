package dev.chain.vpn.ui.main.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chain.vpn.ui.main.MainMoreMenuAction

/**
 * Chain-styled management sheet for the servers screen: a bottom sheet with
 * icon rows instead of the plain text dropdown used before.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChainManageSheet(
    onDismiss: () -> Unit,
    onSelect: (MainMoreMenuAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MainMoreMenuAction.entries.forEach { entry ->
                val accent = if (entry.destructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.secondary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(entry) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(entry.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (entry.destructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
