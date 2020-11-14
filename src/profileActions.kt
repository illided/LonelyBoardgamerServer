package com.twoilya.lonelyboardgamer

import com.twoilya.lonelyboardgamer.auth.AuthenticationException
import com.twoilya.lonelyboardgamer.tables.BGCategories
import com.twoilya.lonelyboardgamer.tables.BGMechanics
import com.twoilya.lonelyboardgamer.tables.UserUtils
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
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
                    ?: throw AuthenticationException("Such user does not exist")
                call.respond(user)
            }

            post("/logout") {
                call.principal<Ticket>()?.id?.let {
                    UserUtils.logOut(it)
                }
                    ?: throw AuthenticationException("Such user does not exist")
                call.respond(ServerResponse(1, "Logged out"))
            }

            route("/change") {
                post("/description") {
                    val new = call.receiveParameters()["new"] ?: throw IllegalArgumentException("No description provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[description] = new
                            }
                        }
                    }
                    call.respond(ServerResponse(1, "Description changed"))
                }

                post("/prefCategories") {
                    val new = call.receiveParameters()["new"]
                        ?.split(",") ?: throw IllegalArgumentException("No categories provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[prefCategories] = BGCategories.findByName(new).joinToString(",")
                            }
                        }
                    }
                    call.respond(ServerResponse(1, "Preferable categories changed"))
                }

                post("/prefMechanics") {
                    val new = call.receiveParameters()["new"]
                        ?.split(",") ?: throw IllegalArgumentException("No mechanics provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[prefMechanics] = BGMechanics.findByName(new).joinToString(",")
                            }
                        }
                    }
                    call.respond(ServerResponse(1, "Preferable mechanics changed"))
                }

                post("/address") {
                    val new = call.receiveParameters()["new"] ?: throw IllegalArgumentException("No address provided")
                    call.principal<Ticket>()?.id?.let {
                        transaction {
                            UsersProfileInfo.update({ UsersProfileInfo.id eq it }) {
                                it[address] = new
                            }
                        }
                    }
                    call.respond(ServerResponse(1, "Address changed"))
                }
            }
        }
    }
}
