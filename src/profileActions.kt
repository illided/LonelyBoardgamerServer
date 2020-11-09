package com.twoilya.lonelyboardgamer

import com.twoilya.lonelyboardgamer.auth.AuthenticationException
import com.twoilya.lonelyboardgamer.tables.UserUtils
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.response.respond
import io.ktor.routing.*

fun profileActions(route: Route) {
    route {
        route("/profile") {
            get("") {
                val user = call.principal<Ticket>()?.id?.let { UsersProfileInfo.find(it) }
                    ?: throw AuthenticationException("Such user does not exist")
                call.respond(user)
            }

            post("/logout") {
                call.principal<Ticket>()?.id?.let { UserUtils.logOut(it) }
                    ?: throw AuthenticationException("Such user does not exist")
                call.respond(ServerResponse(1, "Logged out"))
            }
        }
    }
}