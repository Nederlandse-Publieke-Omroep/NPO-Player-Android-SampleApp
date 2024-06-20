package nl.npo.player.sampleApp.domain

import nl.npo.player.sampleApp.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.domain.model.TokenResponse

interface TokenProvider {
    suspend fun createToken(
        prid: String,
        asPlusUser: Boolean = true,
    ): StreamInfoResult<TokenResponse>
}
