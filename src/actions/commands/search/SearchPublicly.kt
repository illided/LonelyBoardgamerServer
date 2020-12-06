package com.twoilya.lonelyboardgamer.actions.commands.search

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.ResultRow

object SearchPublicly : TableCommand<RelativeProfileInfo?>() {
    private val mapper: (ResultRow) -> RelativeProfileInfo? = { row ->
        RelativeProfileInfo(
            id = row[UsersProfileInfo.id],

            firstName = row[UsersProfileInfo.firstName],
            secondName = row[UsersProfileInfo.secondName],

            description = row[UsersProfileInfo.description] ?: "",

            prefCategories = BGCategories.findById(row[UsersProfileInfo.prefCategories]?.split(",")),
            prefMechanics = BGMechanics.findById(row[UsersProfileInfo.prefMechanics]?.split(","))
        )
    }

    override fun query(userId: Long?, parameters: Parameters): RelativeProfileInfo? {
        require(userId != null)
        { "Search is user specific operation but no user id provided" }

        val searchTarget = parameters["id"]?.toLong()
            ?: throw InfoMissingException("No id provided")

        return UsersProfileInfo.findInTable(
            searchTarget,
            UsersProfileInfo.id,
            mapper
        )
    }
}