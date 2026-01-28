package nl.npo.player.sampleApp.shared.domain

import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.TokenResponse

interface TokenProvider {
    suspend fun createToken(
        prid: String,
        asPlusUser: Boolean = true,
        ageProfile: Int = 18,
    ): StreamInfoResult<TokenResponse>
}
