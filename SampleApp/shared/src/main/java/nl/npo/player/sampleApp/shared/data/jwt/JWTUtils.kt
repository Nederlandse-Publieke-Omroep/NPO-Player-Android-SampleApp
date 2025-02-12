package nl.npo.player.sampleApp.shared.data.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JWTUtils
    @Inject
    constructor() {
        fun getJWTsWithClaims(
            claims: Map<String, Any>,
            secretKey: String,
        ): String {
            val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
            return Jwts
                .builder()
                .setHeader(JWT_HEADER)
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact()
        }

        companion object {
            private val JWT_HEADER = mapOf<String, Any>("alg" to "HS256", "typ" to "JWT")
        }
    }
