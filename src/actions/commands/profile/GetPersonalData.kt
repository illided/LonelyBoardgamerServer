package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.tables.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.ResultRow

object GetPersonalData : TableCommand<ProfileInfo>() {
    private val mapper: (ResultRow) -> ProfileInfo = { row ->
        ProfileInfo(
            id = row[UsersProfileInfo.id],

            firstName = row[UsersProfileInfo.firstName],
            secondName = row[UsersProfileInfo.secondName],

            address = row[UsersProfileInfo.address],

            description = row[UsersProfileInfo.description] ?: "",

            prefCategories = BGCategories.findById(row[UsersProfileInfo.prefCategories]?.split(",")),
            prefMechanics = BGMechanics.findById(row[UsersProfileInfo.prefMechanics]?.split(","))
        )
    }

    override fun query(userId: String, parameters: Parameters): ProfileInfo {
        return UsersProfileInfo.findInTable(
            userId,
            UsersProfileInfo.id,
            mapper
        ) ?: throw ElementWasNotFoundException("No user with such id")
    }
}