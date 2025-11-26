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
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    dialogDescription: String? = "",
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        title = { Text(dialogTitle) },
        text = { Text(dialogDescription ?: "") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.alert_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.alert_dialog_dismiss))
            }
        },
        modifier = modifier,
        onDismissRequest = {},
    )
}
