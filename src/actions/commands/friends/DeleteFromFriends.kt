package com.twoilya.lonelyboardgamer.actions.commands.friends

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import com.twoilya.lonelyboardgamer.tables.FriendStatus
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import io.ktor.http.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import kotlin.math.max
import kotlin.math.min

object DeleteFromFriends : TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        require(userId != null)
        { "Delete from friends is user specific but no user id provided" }

        val target = (parameters["targetId"]
            ?: throw InfoMissingException("No target id provided"))
            .toLongOrNull()
            ?: throw WrongDataFormatException("Wrong format for target id")

        val (person1, person2) = min(userId, target) to max(userId, target)

        val isAffected = UsersRelations.update({
            (UsersRelations.first eq person1) and
                    (UsersRelations.second eq person2) and
                    (UsersRelations.status eq FriendStatus.Friends)
        }) {
            it[status] = FriendStatus.Hidden
            it[actionUser] = target
        } == 1

        if (!isAffected) {
            throw ElementWasNotFoundException("No relations were found")
        }
    }
}