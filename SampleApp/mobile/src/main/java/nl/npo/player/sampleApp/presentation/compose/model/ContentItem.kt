package nl.npo.player.sampleApp.presentation.compose.model

data class ContentItem(
    val id: String,
    val title: String,
    val channel: String,   // orange line
    val codeLine: String,  // grey id line
    val extra: String? = null,
    val imageUrl: String? = null
)
