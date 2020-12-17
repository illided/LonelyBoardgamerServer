package com.twoilya.lonelyboardgamer.actions.commands.friends

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import com.twoilya.lonelyboardgamer.tables.FriendStatus
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import com.twoilya.lonelyboardgamer.tables.findInTable
import io.ktor.http.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import kotlin.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

object AnswerRequest : TableCommand<String>() {
    override fun query(userId: Long?, parameters: Parameters): String {
        require(userId != null)
        { "Answer request is user specific but no user id provided" }

        val answer = (parameters["with"]
            ?: throw InfoMissingException("No answer provided"))
            .toIntOrNull()
            ?: throw WrongDataFormatException("Wrong format for request answer")

        val target = (parameters["targetId"]
            ?: throw InfoMissingException("No target id provided"))
            .toLongOrNull()
            ?: throw WrongDataFormatException("Wrong format for target id")

        val newStatus = when (answer) {
            1 -> FriendStatus.Friends
            0 -> FriendStatus.Hidden
            else -> throw BadDataException("Wrong code for answer")
        }

        val (person1, person2) = min(userId, target) to max(userId, target)

        val isAffected = UsersRelations.update({
            (UsersRelations.first eq person1) and
                    (UsersRelations.second eq person2) and
                    (UsersRelations.actionUser eq target)
        }) {
            it[status] = newStatus
            it[actionUser] = target
        } == 1

        if (isAffected) {
            return if (newStatus == FriendStatus.Friends) {
                UsersProfileInfo.findInTable(
                    target, UsersProfileInfo.id
                ) { it[UsersProfileInfo.VKid].toString() }
                    ?: throw IllegalArgumentException("User deleted while processing")
            } else {
                "Request hidden"
            }
        } else {
            throw ElementWasNotFoundException("No request in context")
        }
    }
}