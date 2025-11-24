package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomAlertDialog(
    dialogTitle: String,
    onDismiss: () -> Unit,
    dialogDescription: String? = "",
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
        onDismissRequest = {},
    )
}
