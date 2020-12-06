package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.tables.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.ResultRow

object GetPersonalData : TableCommand<PersonalProfileInfo>() {
    private val mapper: (ResultRow) -> PersonalProfileInfo = { row ->
        PersonalProfileInfo(
            firstName = row[UsersProfileInfo.firstName],
            secondName = row[UsersProfileInfo.secondName],

            address = row[UsersProfileInfo.address],

            description = row[UsersProfileInfo.description] ?: "",

            prefCategories = BGCategories.findById(row[UsersProfileInfo.prefCategories]?.split(",")),
            prefMechanics = BGMechanics.findById(row[UsersProfileInfo.prefMechanics]?.split(","))
        )
    }

    override fun query(userId: Long?, parameters: Parameters): PersonalProfileInfo {
        require(userId != null)
        { "Get personal data is user specific but no user id provided" }

        return UsersProfileInfo.findInTable(
            userId,
            UsersProfileInfo.id,
            mapper
        ) ?: throw ElementWasNotFoundException("No user with such id")
    }
}