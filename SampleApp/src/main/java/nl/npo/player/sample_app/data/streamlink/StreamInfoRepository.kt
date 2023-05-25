package nl.npo.player.sample_app.data.streamlink

import nl.npo.player.sample_app.BuildConfig
import nl.npo.player.sample_app.data.jwt.JWTUtils
import nl.npo.player.sample_app.domain.TimeProvider
import nl.npo.player.sample_app.domain.TokenProvider
import nl.npo.player.sample_app.domain.model.StreamInfoResult
import nl.npo.player.sample_app.domain.model.TokenResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamInfoRepository @Inject constructor(
    private val jwtUtils: JWTUtils,
    private val timeProvider: TimeProvider
) : TokenProvider {
    /**
     * This is an example of a function that should be done in your backend. In your Android application you make a call to your backend for this token generation!
     */
    override suspend fun createToken(
        prid: String,
        asPlusUser: Boolean
    ): StreamInfoResult<TokenResponse> {
        val claims = mapOf<String, Any>(
            // The (unix) timestamp in seconds when this request is made. This is important so the resulting token can be only used for a limited time.
            "iat" to timeProvider.currentTimeSeconds(),
            // The unique id (PRID or MID in some cases) by which the content is known in the backend.
            "sub" to prid,
            // An issuer should always be added to your signing. Currently only NPO Start has two different issuers so this logic only applies to NPO Start.
            "iss" to if (asPlusUser) BuildConfig.TOKEN_ISSUER_PLUS else BuildConfig.TOKEN_ISSUER_START
        )
        return StreamInfoResult.Success(
            data = TokenResponse(
                jwtUtils.getJWTsWithClaims(
                    claims,
                    if (asPlusUser) BuildConfig.TOKEN_SIGNATURE_PLUS else BuildConfig.TOKEN_SIGNATURE_START
                )
            )
        )
    }
}
