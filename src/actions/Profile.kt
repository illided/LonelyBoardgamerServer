package com.twoilya.lonelyboardgamer.actions

import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.ServerResponse
import com.twoilya.lonelyboardgamer.Ticket
import com.twoilya.lonelyboardgamer.tables.BGCategories
import com.twoilya.lonelyboardgamer.tables.BGMechanics
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import com.twoilya.lonelyboardgamer.actions.commands.profile.LogOut
import com.twoilya.lonelyboardgamer.actions.commands.profile.ChangeAddress
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun profileActions(route: Route) {
    route {
        route("/profile") {
            get("") {
                val user = call.principal<Ticket>()?.id?.let {
                    UsersProfileInfo.find(it)
                }
                    ?: throw ElementWasNotFoundException("Such user does not exist")
                call.respond(user)
            }

            post("/logout") {
                call.principal<Ticket>()?.id?.let {
                    LogOut.execute(it)
                }
                    ?: throw ElementWasNotFoundException("Such user does not exist")
                call.respond(ServerResponse(0, "Logged out"))
            }

            route("/change") {
                post("/description") {
                    val new = call.receiveParameters()["new"] ?: throw InfoMissingException(
                        "No description provided"
                    )
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[description] = new
                            }
                        }
                    }
                    call.respond(
                        ServerResponse(
                            0,
                            "Description changed"
                        )
                    )
                }

                post("/prefCategories") {
                    val new = call.receiveParameters()["new"]
                        ?.split(",") ?: throw InfoMissingException("No categories provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[prefCategories] = BGCategories.findByName(new).joinToString(",")
                            }
                        }
                    }
                    call.respond(
                        ServerResponse(
                            0,
                            "Preferable categories changed"
                        )
                    )
                }

                post("/prefMechanics") {
                    val new = call.receiveParameters()["new"]
                        ?.split(",") ?: throw InfoMissingException("No mechanics provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[prefMechanics] = BGMechanics.findByName(new).joinToString(",")
                            }
                        }
                    }
                    call.respond(
                        ServerResponse(
                            0,
                            "Preferable mechanics changed"
                        )
                    )
                }

                post("/address") {
                    call.principal<Ticket>()?.id?.let {
                        ChangeAddress.execute(it, call.receiveParameters())
                    }
                    call.respond(ServerResponse(0, "Address changed"))
                }
            }
        }
    }
}
