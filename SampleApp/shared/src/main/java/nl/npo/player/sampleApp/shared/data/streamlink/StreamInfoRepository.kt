package nl.npo.player.sampleApp.shared.data.streamlink

import android.util.Log
import kotlinx.coroutines.flow.first
import nl.npo.player.sampleApp.shared.BuildConfig
import nl.npo.player.sampleApp.shared.data.jwt.JWTUtils
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TimeProvider
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.TokenResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamInfoRepository
    @Inject
    constructor(
        private val jwtUtils: JWTUtils,
        private val timeProvider: TimeProvider,
        private val settingsRepository: SettingsRepository,
    ) : TokenProvider {
        /**
         * This is an example of a function that should be done in your backend. In your Android application you make a call to your backend for this token generation!
         */
        override suspend fun createToken(
            prid: String,
            asPlusUser: Boolean,
            ageProfile: Int,
        ): StreamInfoResult<TokenResponse> {
            val claims =
                mapOf<String, Any>(
                    // The (unix) timestamp in seconds when this request is made. This is important so the resulting token can be only used for a limited time.
                    "iat" to timeProvider.currentTimeSeconds(),
                    // The unique id (PRID or MID in some cases) by which the content is known in the backend.
                    "sub" to prid,
                    // An issuer should always be added to your signing. Currently only NPO Start has two different issuers so this logic only applies to NPO Start.
                    "iss" to if (asPlusUser) BuildConfig.TOKEN_ISSUER_PLUS else BuildConfig.TOKEN_ISSUER_START,
                    "age_profile" to ageProfile,
                )
            val environment = settingsRepository.environment.first()
            return StreamInfoResult.Success(
                data =
                    TokenResponse(
                        jwtUtils
                            .getJWTsWithClaims(
                                claims,
                                when (environment) {
                                    Environment.Test -> {
                                        if (asPlusUser) {
                                            BuildConfig.TOKEN_SIGNATURE_PLUS_TEST
                                        } else {
                                            BuildConfig.TOKEN_SIGNATURE_START_TEST
                                        }
                                    }

                                    Environment.Acceptance -> {
                                        if (asPlusUser) {
                                            BuildConfig.TOKEN_SIGNATURE_PLUS_ACC
                                        } else {
                                            BuildConfig.TOKEN_SIGNATURE_START_ACC
                                        }
                                    }

                                    Environment.Production -> {
                                        if (asPlusUser) {
                                            BuildConfig.TOKEN_SIGNATURE_PLUS_PROD
                                        } else {
                                            BuildConfig.TOKEN_SIGNATURE_START_PROD
                                        }
                                    }
                                },
                            ).also { Log.d("SampleAppTest", "JWT: $it") },
                    ),
            )
        }
    }
