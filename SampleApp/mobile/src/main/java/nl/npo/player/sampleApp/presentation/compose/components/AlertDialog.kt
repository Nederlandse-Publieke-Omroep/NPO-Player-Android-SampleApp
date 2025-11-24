package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CustomAlertDialog(
    dialogTitle: String,
    onDismiss: () -> Unit,
    dialogDescription: String? = "",
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var dialogVisible by remember { mutableStateOf(false) }

    fun closeDialog() {
        dialogVisible = false
    }

    if (dialogVisible) return

    AlertDialog(
        title = { Text(dialogTitle) },
        text = { Text(dialogDescription ?: "") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Close")
            }
        },
        modifier = modifier,
        onDismissRequest = ::closeDialog,
    )
}
