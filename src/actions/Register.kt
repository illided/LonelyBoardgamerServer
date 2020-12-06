package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.actions.commands.register.AddUser
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.registerRoute() {
    post("/register") {
        val parameters = call.receiveParameters()
        call.respond(ServerResponse(0, AddUser.run(parameters = parameters)))
    }
}