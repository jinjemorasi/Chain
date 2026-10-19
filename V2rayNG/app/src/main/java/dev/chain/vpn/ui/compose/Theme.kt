package dev.chain.vpn.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.chain.vpn.AppConfig
import dev.chain.vpn.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ---- Chain brand: deep purple primary + amber accent on near-black surfaces ----

private val ChainAmber = Color(0xFFFFB300)
private val ChainAmberDark = Color(0xFFFFC94D)
private val ChainViolet = Color(0xFF8B5CF6)
private val ChainDeepPurple = Color(0xFF6D28D9)
private val ChainGreen = Color(0xFF34D399)

private val LightColor = lightColorScheme(
    primary = ChainDeepPurple,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DEFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF9A6200),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDF9E),
    onSecondaryContainer = Color(0xFF2A1A00),
    tertiary = Color(0xFF006B4F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF8FF5C8),
    onTertiaryContainer = Color(0xFF002117),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF9FF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFBF9FF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE7E0F2),
    onSurfaceVariant = Color(0xFF4A4458),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF),
    scrim = Color(0xFF000000),
    surfaceTint = ChainDeepPurple,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F2FA),
    surfaceContainer = Color(0xFFF0ECF5),
    surfaceContainerHigh = Color(0xFFEAE6F0),
    surfaceContainerHighest = Color(0xFFE4DFEA),
)

private val DarkColor = darkColorScheme(
    primary = ChainViolet,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFE9E2FF),
    secondary = ChainAmber,
    onSecondary = Color(0xFF221600),
    secondaryContainer = Color(0xFF5C4100),
    onSecondaryContainer = Color(0xFFFFE7B3),
    tertiary = ChainGreen,
    onTertiary = Color(0xFF002117),
    tertiaryContainer = Color(0xFF00513B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B0B10),
    onBackground = Color(0xFFF1EEF7),
    surface = Color(0xFF121218),
    onSurface = Color(0xFFF1EEF7),
    surfaceVariant = Color(0xFF23232B),
    onSurfaceVariant = Color(0xFFC9C5D6),
    outline = Color(0xFF8E8A99),
    outlineVariant = Color(0xFF35353D),
    inverseSurface = Color(0xFFF1EEF7),
    inverseOnSurface = Color(0xFF1A1A20),
    inversePrimary = ChainDeepPurple,
    scrim = Color(0xFF000000),
    surfaceTint = ChainViolet,
    surfaceContainerLowest = Color(0xFF08080C),
    surfaceContainerLow = Color(0xFF101016),
    surfaceContainer = Color(0xFF15151C),
    surfaceContainerHigh = Color(0xFF1C1C24),
    surfaceContainerHighest = Color(0xFF23232C),
)

// Semantic Colors
val colorPing = ChainGreen // Green
val colorPingRed = Color(0xFFFF5C7A) // Red-pink
val colorPingMid = ChainAmberDark // Amber
val colorPingUntested = Color(0xFF8E8A99) // Gray
val colorConfigType = ChainAmberDark // Amber
val colorFabActive = ChainAmberDark // Amber
val colorFabInactiveLight = Color(0xFF9C9C9C) // Gray
val colorFabInactiveDark = Color(0xFF646464) // Dark Gray
val dividerColorLight = Color(0xFFE0E0E0) // Light Gray
val dividerColorDark = Color(0xFF2A2A32) // Dark Gray
val colorConnectedGlow = ChainGreen // Green
val colorConnectingGlow = ChainAmberDark // Amber

// Toast Colors 70%
val toastNormalBgLight = Color(0xB3353A3E) // Dark Gray
val toastNormalBgDark = Color(0xB34A4F54) // Darker Gray
val toastSuccessBg = Color(0xB3388E3C) // Green
val toastErrorBg = Color(0xB3D50000) // Red
val toastInfoBg = Color(0xB33F51B5) // Indigo Blue
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        // Chain is dark-first: fresh installs start in dark mode.
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        // Chain uses its fixed brand palette; Material You dynamic color is off.
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
