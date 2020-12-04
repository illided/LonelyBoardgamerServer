package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import com.twoilya.lonelyboardgamer.tables.dbQuery
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.update

object ChangeDescription : TableCommand<Unit>() {
    override fun query(userId: String, parameters: Parameters) {
        val new = parameters["new"]
            ?: throw InfoMissingException("No description provided")

        if (new.length > UsersProfileInfo.DESCRIPTION_MAX_LENGTH)
            throw WrongDataFormatException("Description too big")

        UsersProfileInfo.update({ UsersProfileInfo.id eq userId }) {
            it[description] = new
        }
    }
}