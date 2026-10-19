package dev.chain.vpn.ui.shortcut

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chain.vpn.core.CoreServiceManager
import dev.chain.vpn.core.LauncherManager
import dev.chain.vpn.ui.base.BaseComponentActivity

class ScStartActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        LaunchedEffect(Unit) {
            moveTaskToBack(true)
            if (!CoreServiceManager.isRunning()) {
                LauncherManager.startServiceFromToggle(this@ScStartActivity)
            }
            finish()
        }
    }
}
