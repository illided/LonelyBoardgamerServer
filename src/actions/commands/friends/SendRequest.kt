package com.twoilya.lonelyboardgamer.actions.commands.friends

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.FriendStatus
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import io.ktor.http.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.postgresql.util.PSQLException
import java.sql.SQLException
import kotlin.math.max
import kotlin.math.min

object SendRequest : TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        val targetId = parameters["targetId"]?.toLongOrNull()
            ?: throw InfoMissingException("No target id provided")

        require(userId != null)
        { "Send request is user specific but no user id provided" }

        val (person1, person2) = min(userId, targetId) to max(userId, targetId)

        try {
            UsersRelations.insert {
                it[first] = person1
                it[second] = person2
                it[status] = FriendStatus.Pending
                it[actionUser] = userId
            }
        } catch (exception: ExposedSQLException) {
            throw BadDataException(
                "Invalid parameters for request (such user does not exist " +
                        "or some relations already in place)"
            )
        }
    }
}
