package com.twoilya.lonelyboardgamer

import com.fasterxml.jackson.databind.SerializationFeature
import com.twoilya.lonelyboardgamer.actions.profileActions
import com.twoilya.lonelyboardgamer.actions.searchActions
import com.twoilya.lonelyboardgamer.auth.JwtConfig
import com.twoilya.lonelyboardgamer.actions.loginRoute
import com.twoilya.lonelyboardgamer.actions.registerRoute
import com.twoilya.lonelyboardgamer.tables.DatabaseConnector
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import io.github.cdimascio.dotenv.dotenv
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    val port = dotenv["PORT"]?.toInt() ?: 23567

    embeddedServer(Netty, port) {
        DatabaseConnector.init()

        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        install(StatusPages) {
            exception<Throwable> { cause ->
                if (cause is WithCodeException) {
                    call.respond(ServerResponse(cause.code, cause.message ?: "No message provided"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Unexpected exception caught on server")
                    println("UNEXPECTED EXCEPTION: ${cause.message}")
                }
            }
        }

        install(Authentication) {
            jwt {
                verifier(JwtConfig.verifier)
                realm = "ktor.io"
                validate { jwtCredential ->
                    val id = jwtCredential.payload.claims["id"]?.asString() ?: return@validate null
                    val iat = jwtCredential.payload.claims["iat"]?.asDate() ?: return@validate null
                    if (UsersLoginInfo.isUserLoggedIn(id, iat)) {
                        Ticket(id)
                    } else {
                        null
                    }
                }
            }
        }

        install(Routing) {
            loginRoute(this)
            registerRoute(this)
            authenticate {
                profileActions(this)
                searchActions(this)
            }
        }
    }.start(wait = true)
}

