package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.AuthorizationException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.actions.commands.register.AddUser
import com.twoilya.lonelyboardgamer.auth.JwtConfig
import com.twoilya.lonelyboardgamer.auth.LoggedInService
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.registerRoute() {
    post("/register") {
        val parameters = call.receiveParameters()

        val vkAccessToken = parameters["VKAccessToken"] ?: throw InfoMissingException("No token provided")
        val userId = VKConnector.checkToken(vkAccessToken)

        if (LoggedInService.isExist(userId)) {
            throw AuthorizationException("This user already exist")
        }
        AddUser.execute(userId, parameters)

        call.respond(ServerResponse(0, JwtConfig.makeToken(userId)))
    }
}