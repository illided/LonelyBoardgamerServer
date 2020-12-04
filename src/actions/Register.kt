package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.AuthorizationException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.actions.commands.register.AddUser
import com.twoilya.lonelyboardgamer.auth.JwtConfig
import com.twoilya.lonelyboardgamer.auth.isExist
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.registerRoute() {
    post("/register") {
        val parameters = call.receiveParameters()

        val vkAccessToken = parameters["VKAccessToken"] ?: throw InfoMissingException("No token provided")
        val userId = VKConnector.checkToken(vkAccessToken)

        if (isExist(userId)) {
            throw AuthorizationException("This user already exist")
        }
        AddUser.run(userId, parameters)

        call.respond(ServerResponse(0, JwtConfig.makeToken(userId)))
    }
}