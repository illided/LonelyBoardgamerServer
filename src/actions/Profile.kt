package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.Ticket
import com.twoilya.lonelyboardgamer.actions.commands.profile.*
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.profileActions() {
    route("/profile") {
        get("") {
            val user = call.principal<Ticket>()?.id!!
            call.respond(ServerResponse(0, GetPersonalData.run(user)))
        }

        post("/logout") {
            val user = call.principal<Ticket>()?.id!!
            LogOut.run(user)
            call.respond(ServerResponse(0, "Logged out"))
        }

        route("/change") {
            post("/description") {
                ChangeDescription.run(
                    call.principal<Ticket>()?.id!!,
                    call.receiveParameters()
                )
                call.respond(ServerResponse(0, "Description changed"))
            }

            post("/prefCategories") {
                ChangeCategories.run(
                    call.principal<Ticket>()?.id!!,
                    call.receiveParameters()
                )
                call.respond(ServerResponse(0, "Preferable categories changed"))
            }

            post("/prefMechanics") {
                ChangeMechanics.run(
                    call.principal<Ticket>()?.id!!,
                    call.receiveParameters()
                )
                call.respond(ServerResponse(0, "Preferable mechanics changed"))
            }

            post("/address") {
                ChangeAddress.run(
                    call.principal<Ticket>()?.id!!,
                    call.receiveParameters()
                )
                call.respond(ServerResponse(0, "Address changed"))
            }
        }
    }
}
