package dev.chain.vpn.ui.main.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chain.vpn.BuildConfig
import dev.chain.vpn.R
import dev.chain.vpn.ui.main.MainDestination

private data class SettingsSection(
    @StringRes val titleRes: Int,
    val destinations: List<MainDestination>,
)

private val sections = listOf(
    SettingsSection(
        R.string.chain_settings_section_servers,
        listOf(MainDestination.Subscriptions),
    ),
    SettingsSection(
        R.string.chain_settings_section_proxy,
        listOf(MainDestination.PerAppProxy, MainDestination.Routing),
    ),
    SettingsSection(
        R.string.chain_settings_section_tools,
        listOf(
            MainDestination.UserAssets,
            MainDestination.Logcat,
            MainDestination.BackupRestore,
            MainDestination.CheckUpdate,
        ),
    ),
    SettingsSection(
        R.string.chain_settings_section_app,
        listOf(
            MainDestination.Settings,
            MainDestination.Promotion,
            MainDestination.About,
        ),
    ),
)

@Composable
fun ChainSettingsScreen(
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "v" + BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        sections.forEach { section ->
            item(key = "section-${section.titleRes}") {
                Text(
                    text = stringResource(section.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            items(section.destinations, key = { it.name }) { destination ->
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(destination.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(destination.iconRes),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onNavigate(destination) },
                    )
                }
            }
            item(key = "divider-${section.titleRes}") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}
