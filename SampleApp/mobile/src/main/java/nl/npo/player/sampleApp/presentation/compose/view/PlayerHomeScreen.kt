package nl.npo.player.sampleApp.presentation.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nl.npo.player.sampleApp.presentation.compose.ContentList
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@Composable
fun PlayerHomeScreen(
    liveItems: SourceWrapper,
    vodItems: SourceWrapper,
    onItemClick: (SourceWrapper) -> Unit,
) {
    // Dark surface like the screenshot
    Surface(color = Color(0xFF0C0C0C)) {
        Box(Modifier.fillMaxSize()) {
            ContentList(
                live = listOf(liveItems),
                vod = listOf(vodItems),
                onItemClick = onItemClick
            )
        }
    }
}
