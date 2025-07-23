package nl.npo.player.sampleApp.shared.app

import nl.npo.tag.sdk.NpoTag

interface PlayerApplication {
    val brandId: Int
        get() = 634226
    var npoTag: NpoTag?

    fun isPlayerInitiatedYet(): Boolean

    suspend fun initiatePlayerLibrary(withNPOTag: Boolean)
}
