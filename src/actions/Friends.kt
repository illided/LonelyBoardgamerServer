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
            call.respond(
                ServerResponse(
                    0,
                    GetFriendList.run(
                        call.principal<Ticket>()?.id,
                        call.parameters
                    )
                )
            )
        }

        route("/requests") {
            get("/toMe") {
                call.respond(
                    ServerResponse(
                        0,
                        GetToMeRequests.run(
                            call.principal<Ticket>()?.id,
                            call.parameters
                        )
                    )
                )
            }

            get("/fromMe") {
                call.respond(
                    ServerResponse(
                        0,
                        GetFromMeRequests.run(
                            call.principal<Ticket>()?.id,
                            call.parameters
                        )
                    )
                )
            }

            get("/hidden") {
                call.respond(
                    ServerResponse(
                        0,
                        GetHiddenRequests.run(
                            call.principal<Ticket>()?.id,
                            call.parameters
                        )
                    )
                )
            }

            post("/answer") {
                call.respond(
                    ServerResponse(
                        0, AnswerRequest.run(
                            call.principal<Ticket>()?.id,
                            call.parameters
                        )
                    )
                )
            }

            post("/send") {
                SendRequest.run(
                    call.principal<Ticket>()?.id,
                    call.parameters
                )
                call.respond(ServerResponse(0, "Request sent"))
            }
            post("/withdraw") {

            }
        }
    }
}