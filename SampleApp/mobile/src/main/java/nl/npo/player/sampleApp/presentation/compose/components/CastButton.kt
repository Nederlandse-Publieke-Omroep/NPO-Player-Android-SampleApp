package nl.npo.player.sampleApp.presentation.compose.components

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
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
fun CastButton(activity: Activity) {
    AndroidView(
        factory = { context ->
            MediaRouteButton(context).apply {
                CastButtonFactory.setUpMediaRouteButton(activity, this)
            }
        },
        update = { /* no-op */ },
    )
}

@Preview(showBackground = true)
@Composable
fun CastButtonPreview() {
    val activity = LocalActivity.current as? AppCompatActivity ?: return
    MaterialTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp), // ensures the AndroidView is visible
            contentAlignment = Alignment.Center
        ) {
            CastButton(activity)
        }
    }
}