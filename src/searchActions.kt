package com.twoilya.lonelyboardgamer

import com.twoilya.lonelyboardgamer.tables.UsersLocations
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun searchActions(route: Route) {
    route {
        route("/search") {
            get("") {
                call.principal<Ticket>()?.id?.let {
                    call.respond(
                        ServerResponse(
                            0,
                            UsersLocations.findNearest(
                                it,
                                call.parameters["limit"]?.toIntOrNull()
                                    ?: throw WrongDataFormatException("Wrong format for limit parameter"),
                                call.parameters["offset"]?.toIntOrNull()
                                    ?: throw WrongDataFormatException("Wrong format for offset parameter")
                            )
                        )
                    )
                }
            }
            get("/byId") {
                call.principal<Ticket>()?.id?.let {
                    call.respond(
                        UsersProfileInfo.find(it)?.onlyPublic()
                            ?: throw ElementWasNotFoundException("No user with such id")
                    )
                }
            }
        }
    }
}