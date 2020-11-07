package com.twoilya.lonelyboardgamer.auth

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.tables.UserUtils
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun loginRoute(route: Routing) {
    route {
        post("/login") {
            val user = call.receive<User>()
            require(user.VKAccessToken != null) { "No token provided" }

            val userId = VKAuth.checkToken(user.VKAccessToken)
            if (!UserUtils.isExist(userId)) {
                throw AuthorizationException(
                    "This user does not exist"
                )
            }

            call.respond(ServerResponse(1, JwtConfig.makeToken(userId)))
        }
    }
}

fun registerRoute(route: Routing) {
    route {
        post("/register") {
            val user = call.receive<User>()
            require(user.VKAccessToken != null) { "No token provided" }

            val userId = VKAuth.checkToken(user.VKAccessToken)
            if (UserUtils.isExist(userId)) {
                throw AuthorizationException("This user already exist")
            }

            val (firstName, secondName) = VKAuth.getName(userId)

            UserUtils.addUser(
                User(
                    first_name = firstName,
                    last_name = secondName,
                    id = userId,
                    address = user.address
                )
            )
            call.respond(ServerResponse(1, JwtConfig.makeToken(userId)))
        }
    }
}