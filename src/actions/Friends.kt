package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.Ticket
import com.twoilya.lonelyboardgamer.actions.commands.friends.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.friendsActions() {
    route("/friends") {
        get("") {
            val user = call.principal<Ticket>()?.id!!
            call.respond(
                ServerResponse(
                    0,
                    GetFriendList.run(user, call.parameters)
                )
            )
        }
        route("/requests") {
            get("/toMe") {

            }
            get("/fromMe") {

            }
            get("/hidden") {

            }
            post("/answer") {

            }
            post("/send") {

            }
            post("/withdraw") {

            }
        }
    }
}