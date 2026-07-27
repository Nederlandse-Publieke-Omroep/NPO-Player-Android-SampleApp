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
    confirmButtonText: String = stringResource(R.string.alert_dialog_confirm),
    dismissButtonText: String = stringResource(R.string.alert_dialog_dismiss),
) {
    AlertDialog(
        title = { Text(dialogTitle) },
        text = { Text(dialogDescription ?: "") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            onDismiss?.let { onDismiss ->
                TextButton(onClick = { onDismiss() }) {
                    Text(dismissButtonText)
                }
            }
        },
        modifier = Modifier,
        onDismissRequest = { onDismiss?.invoke() },
    )
}
