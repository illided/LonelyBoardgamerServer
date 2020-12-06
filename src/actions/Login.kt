package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.auth.JwtConfig
import com.twoilya.lonelyboardgamer.auth.getIdQuery
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.loginRoute() {
    post("/login") {
        val parameters = call.receiveParameters()

        val vkAccessToken = parameters["VKAccessToken"]
            ?: throw InfoMissingException("No token provided")

        val vkId = VKConnector.checkToken(vkAccessToken)

        val userId = transaction { getIdQuery(vkId) }
            ?: throw ElementWasNotFoundException("This user does not exist")

        call.respond(ServerResponse(0, JwtConfig.makeToken(userId)))
    }
}

