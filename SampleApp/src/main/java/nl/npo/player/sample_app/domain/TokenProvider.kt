package nl.npo.player.sample_app.domain

import nl.npo.player.sample_app.domain.model.StreamInfoResult
import nl.npo.player.sample_app.domain.model.TokenResponse

interface TokenProvider {
    suspend fun createToken(
        prid: String,
        asPlusUser: Boolean = true
    ): StreamInfoResult<TokenResponse>
}
