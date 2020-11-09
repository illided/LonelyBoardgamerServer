package com.twoilya.lonelyboardgamer

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import com.fasterxml.jackson.databind.*
import com.twoilya.lonelyboardgamer.auth.*
import com.twoilya.lonelyboardgamer.tables.DatabaseConnector
import com.twoilya.lonelyboardgamer.tables.UserUtils
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.github.cdimascio.dotenv.dotenv
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.jackson.*
import io.ktor.features.*
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
                call.respond(
                    ServerResponse(
                        0,
                        cause.message ?: "No message provided"
                    )
                )
            }
        }

        install(Authentication) {
            jwt {
                verifier(JwtConfig.verifier)
                realm = "ktor.io"
                validate { jwtCredential ->
                    val id = jwtCredential.payload.claims["id"]?.asString() ?: return@validate null
                    val iat = jwtCredential.payload.claims["iat"]?.asDate() ?: return@validate null
                    if (UserUtils.isUserLoggedIn(id, iat)) {
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
            }
        }
    }.start(wait = true)
}

