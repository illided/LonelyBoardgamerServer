package com.twoilya.lonelyboardgamer.actions.commands.friends

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.FriendStatus
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import io.ktor.http.*
import org.jetbrains.exposed.sql.insertIgnore
import kotlin.math.max
import kotlin.math.min

object SendRequest: TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        val targetId = parameters["targetId"]?.toLongOrNull()
            ?: throw InfoMissingException("No target id provided")

        require(userId != null)
        {"Send request is user specific but no user id provided"}

        val (person1, person2) = min(userId, targetId) to max(userId, targetId)

        UsersRelations.insertIgnore {
            it[first] = person1
            it[second] = person2
            it[status] = FriendStatus.Pending
            it[actionUser] = userId
        }
    }
}