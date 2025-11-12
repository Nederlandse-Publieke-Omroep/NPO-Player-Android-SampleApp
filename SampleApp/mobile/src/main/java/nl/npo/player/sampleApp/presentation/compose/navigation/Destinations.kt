package nl.npo.player.sampleApp.presentation.compose.navigation

import kotlinx.serialization.Serializable

//object Routes {
//    const val VIDEO_LIST = "video_list"
//    const val AUDIO_LIST = "audio_list"
//    const val DETAIL = "detail"
//}

sealed interface Destinations {
    @Serializable
    data object Player : Destinations

    @Serializable
    data object OfflineList : Destinations
}

