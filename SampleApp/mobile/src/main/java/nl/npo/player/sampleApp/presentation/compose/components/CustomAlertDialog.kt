package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.npo.player.sampleApp.R

@Composable
fun CustomAlertDialog(
    dialogTitle: String,
    onDismiss: (() -> Unit)? = null,
    dialogDescription: String? = "",
    onConfirm: () -> Unit = {},
    confirmText: String = stringResource(R.string.alert_dialog_confirm),
    dismissText: String = stringResource(R.string.alert_dialog_dismiss),
) {
    AlertDialog(
        title = { Text(dialogTitle) },
        text = { Text(dialogDescription ?: "") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            onDismiss?.let { onDismiss ->
                TextButton(onClick = { onDismiss() }) {
                    Text(dismissText)
                }
            }
        },
        modifier = Modifier,
        onDismissRequest = { onDismiss?.invoke() },
    )
}
