package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.Ticket
import com.twoilya.lonelyboardgamer.actions.commands.profile.*
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.response.respond
import io.ktor.routing.*

fun profileActions(route: Route) {
    route {
        route("/profile") {
            get("") {
                val user = call.principal<Ticket>()?.id!!
                call.respond(GetPersonalData.execute(user))
            }

            post("/logout") {
                val user = call.principal<Ticket>()?.id!!
                LogOut.execute(user)
                call.respond(ServerResponse(0, "Logged out"))
            }

            route("/change") {
                post("/description") {
                    ChangeDescription.execute(
                        call.principal<Ticket>()?.id!!,
                        call.parameters
                    )
                    call.respond(ServerResponse(0, "Description changed"))
                }

                post("/prefCategories") {
                    ChangeCategories.execute(
                        call.principal<Ticket>()?.id!!,
                        call.parameters
                    )
                    call.respond(ServerResponse(0, "Preferable categories changed"))
                }

                post("/prefMechanics") {
                    ChangeMechanics.execute(
                        call.principal<Ticket>()?.id!!,
                        call.parameters
                    )
                    call.respond(ServerResponse(0, "Preferable mechanics changed"))
                }

                post("/address") {
                    ChangeAddress.execute(
                        call.principal<Ticket>()?.id!!,
                        call.parameters
                    )
                    call.respond(ServerResponse(0, "Address changed"))
                }
            }
        }
    }
}
