package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.auth.JwtConfig
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import com.twoilya.lonelyboardgamer.actions.commands.register.AddUser
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun loginRoute(route: Routing) {
    route {
        post("/login") {
            val vkAccessToken =
                call.receiveParameters()["VKAccessToken"] ?: throw InfoMissingException("No token provided")
            val userId = VKConnector.checkToken(vkAccessToken)
            if (!UsersLoginInfo.isExist(userId)) {
                throw ElementWasNotFoundException("This user does not exist")
            }
            call.respond(ServerResponse(0,
                JwtConfig.makeToken(userId)
            ))
        }
    }
}

fun registerRoute(route: Routing) {
    route {
        post("/register") {
            val parameters = call.receiveParameters()
            val vkAccessToken = parameters["VKAccessToken"] ?: throw InfoMissingException("No token provided")
            val userId = VKConnector.checkToken(vkAccessToken)
            if (UsersLoginInfo.isExist(userId)) {
                throw ElementWasNotFoundException("This user already exist")
            }
            AddUser()
                .execute(userId, parameters)
            call.respond(ServerResponse(0,
                JwtConfig.makeToken(userId)
            ))
        }
    }
}