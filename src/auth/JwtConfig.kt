package com.twoilya.lonelyboardgamer.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.twoilya.lonelyboardgamer.Ticket
import io.github.cdimascio.dotenv.dotenv
import io.ktor.auth.jwt.JWTCredential
import java.time.Instant
import java.util.*

object JwtConfig {
    private val algorithm = Algorithm.HMAC512(dotenv { ignoreIfMissing = true }["JWT_SECRET"])

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .build()

    fun makeToken(userId: String): String = JWT.create()
        .withClaim("id", userId)
        .withIssuedAt(Date.from(Instant.now()))
        .sign(algorithm)

    suspend fun verifyAndGetPrincipal(jwtCredential: JWTCredential) : Ticket? {
        val id = jwtCredential.payload.claims["id"]?.asString() ?: return null
        val iat = jwtCredential.payload.claims["iat"]?.asDate() ?: return null
        return if (LoggedInService.isUserLoggedIn(id, iat)) {
            Ticket(id)
        } else {
            null
        }
    }
}
