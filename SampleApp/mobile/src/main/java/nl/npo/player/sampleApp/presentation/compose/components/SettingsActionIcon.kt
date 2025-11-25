package nl.npo.player.sampleApp.presentation.compose.components

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import nl.npo.player.sampleApp.presentation.settings.SettingsBottomSheetDialog

@Composable
fun SettingsActionIcon() {
    val activity = LocalActivity.current as? AppCompatActivity ?: return

    IconButton(
        onClick = {
            SettingsBottomSheetDialog.newInstance()
                .show(activity.supportFragmentManager, "SettingsBottomSheet")
        },
    ) {
        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
    }
}
