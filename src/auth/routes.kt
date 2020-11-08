package com.twoilya.lonelyboardgamer.auth

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.vk.VKConnector
import com.twoilya.lonelyboardgamer.tables.UserUtils
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun loginRoute(route: Routing) {
    route {
        post("/login") {
            val vkAccessToken =
                call.receiveParameters()["VKAccessToken"] ?: throw IllegalArgumentException("No token provided")
            val userId = VKConnector.checkToken(vkAccessToken)
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
            val parameters = call.receiveParameters()

            val vkAccessToken = parameters["VKAccessToken"]
            val address = parameters["address"]
            require(vkAccessToken != null && address != null) { "No token provided" }

            val userId = VKConnector.checkToken(vkAccessToken)
            if (UserUtils.isExist(userId)) {
                throw AuthorizationException("This user already exist")
            }

            val (firstName, lastName) = VKConnector.getName(userId)

            UserUtils.addUser(
                userFirstName = firstName,
                userSecondName = lastName,
                userId = userId,
                userAddress = address
            )
            call.respond(ServerResponse(1, JwtConfig.makeToken(userId)))
        }
    }
}