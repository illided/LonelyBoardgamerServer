package com.twoilya.lonelyboardgamer.actions.commands.search

import actions.commands.TableCommand
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.tables.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

object SearchPublicly : TableCommand<RelativeProfileInfo?>() {
    private val mapper: (ResultRow) -> RelativeProfileInfo? = { row ->
        RelativeProfileInfo(
            id = row[UsersProfileInfo.id],

            firstName = row[UsersProfileInfo.firstName],
            secondName = row[UsersProfileInfo.secondName],

            description = row[UsersProfileInfo.description] ?: "",

            prefCategories = BGCategories.findById(row[UsersProfileInfo.prefCategories]?.split(",")),
            prefMechanics = BGMechanics.findById(row[UsersProfileInfo.prefMechanics]?.split(",")),
        )
    }

    private fun getFriendStatusQuery(userId: Long, targetId: Long): RelativeFriendStatus {
        val (person1, person2) = min(userId, targetId) to max(userId, targetId)
        val (actionUser, status) = UsersRelations
            .select { (UsersRelations.first eq person1) and (UsersRelations.second eq person2) }
            .map { it[UsersRelations.actionUser] to it[UsersRelations.status] }
            .also { if (it.size != 1) return RelativeFriendStatus.None }
            .component1()

        return when {
            (actionUser == userId) && (status != FriendStatus.Friends) -> RelativeFriendStatus.RequestSentFromUser
            (actionUser != userId) && (status != FriendStatus.Friends) -> RelativeFriendStatus.RequestSentToUser
            status == FriendStatus.Friends -> RelativeFriendStatus.Friends
            else -> throw IllegalArgumentException("No relative friend status can be associated")
        }
    }

    override fun query(userId: Long?, parameters: Parameters): RelativeProfileInfo? {
        require(userId != null)
        { "Search is user specific operation but no user id provided" }

        val searchTarget = parameters["id"]?.toLong()
            ?: throw InfoMissingException("No id provided")

        var target = UsersProfileInfo.findInTable(
            searchTarget,
            UsersProfileInfo.id,
            mapper
        )

        if (target != null) {
            target = target.withStatus(getFriendStatusQuery(userId, target.id))
            if (target.friendStatus == RelativeFriendStatus.Friends) {
                target = target.withVkID(
                    UsersProfileInfo.findInTable(
                        target.id,
                        UsersProfileInfo.id,
                        { it[UsersProfileInfo.VKid].toString() })
                        ?: throw IllegalArgumentException("User was deleted while processing")
                )
            }
        }
        return target
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RelativeProfileInfo(
    val id: Long,
    val VKid: String? = null,
    val firstName: String,
    val secondName: String,
    val prefCategories: List<String>,
    val prefMechanics: List<String>,
    val description: String,
    val friendStatus: RelativeFriendStatus? = null
) {
    fun withStatus(status: RelativeFriendStatus): RelativeProfileInfo {
        return RelativeProfileInfo(
            id, VKid, firstName, secondName, prefCategories, prefMechanics, description, status
        )
    }

    fun withVkID(vkID: String): RelativeProfileInfo {
        return RelativeProfileInfo(
            id, vkID, firstName, secondName, prefCategories, prefMechanics, description, friendStatus
        )
    }
}

enum class RelativeFriendStatus {
    @JsonProperty("0")
    None,
    @JsonProperty("1")
    RequestSentFromUser,
    @JsonProperty("2")
    RequestSentToUser,
    @JsonProperty("3")
    Friends
}

