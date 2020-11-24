package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.update

object ChangeDescription : TableCommand() {
    suspend fun execute(userId: String, parameters: Parameters) {
        val new = parameters["new"]
            ?: throw InfoMissingException("No description provided")
        dbQuery {
            UsersProfileInfo.update({ UsersProfileInfo.id eq userId }) {
                it[description] = new
            }
        }
    }
}