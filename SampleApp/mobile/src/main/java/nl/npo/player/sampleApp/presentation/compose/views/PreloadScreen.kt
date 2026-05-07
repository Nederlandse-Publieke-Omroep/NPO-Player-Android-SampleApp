package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun PreloadScreen() {
    val listState = rememberLazyListState()

    val snappingLayout = remember {
        SnapLayoutInfoProvider(listState)
    }

    val snappingFlingBehavior = rememberSnapFlingBehavior(
        snapLayoutInfoProvider = snappingLayout
    )

    LazyColumn(
        state = listState,
        flingBehavior = snappingFlingBehavior
    ) {
        // your items here
    }
}
