package com.twoilya.lonelyboardgamer.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.auth.Authentication
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
}
