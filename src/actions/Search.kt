package com.twoilya.lonelyboardgamer.actions

import actions.commands.search.SearchNearest
import com.twoilya.lonelyboardgamer.*
import com.twoilya.lonelyboardgamer.actions.commands.search.SearchPublicly
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.searchActions() {
    route("/search") {
        get("") {
            call.respond(
                try {
                    ServerResponse(0, SearchNearest.run(call.principal<Ticket>()?.id!!, call.parameters))
                } catch (exception: NumberFormatException) {
                    throw WrongDataFormatException("Limit or offset have wrong format")
                }
            )
        }

        get("/byId") {
            call.respond(
                ServerResponse(
                    0, SearchPublicly.run(call.principal<Ticket>()?.id!!, call.parameters)
                        ?: throw ElementWasNotFoundException("No user with such id")
                )
            )
        }
    }
}