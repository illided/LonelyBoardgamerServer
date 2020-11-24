package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.BGMechanics
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.update

object ChangeMechanics: TableCommand() {
    suspend fun execute(userId: String, parameters: Parameters) {
        val new = parameters["new"]?.split(",")
            ?: throw InfoMissingException("No categories provided")

        dbQuery {
            UsersProfileInfo.update({ UsersProfileInfo.id eq userId }) {
                it[prefCategories] = BGMechanics.findByName(new).joinToString(",")
            }
        }
    }
}