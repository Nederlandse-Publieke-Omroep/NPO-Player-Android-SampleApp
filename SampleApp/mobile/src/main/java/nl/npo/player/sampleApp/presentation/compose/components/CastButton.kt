package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
fun CastButton() {
    AndroidView(
        factory = { context ->
            MediaRouteButton(context).apply {
                CastButtonFactory.setUpMediaRouteButton(context, this)
            }
        },
        update = { /* no-op */ },
    )
}



@Preview(showBackground = true)
@Composable
fun CastButtonPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp), // ensures the AndroidView is visible
            contentAlignment = Alignment.Center
        ) {
            CastButton()

        }
    }
}