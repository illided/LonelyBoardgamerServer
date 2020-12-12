package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.BGMechanics
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.update

object ChangeMechanics : TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        require(userId != null)
        { "Change mechanics is user specific but no user id provided" }

        val new = parameters["new"]?.split(",")
            ?: throw InfoMissingException("No categories provided")

        UsersProfileInfo.update({ UsersProfileInfo.id eq userId }) {
            it[prefMechanics] = BGMechanics.findByName(new).joinToString(",")
        }
    }
}