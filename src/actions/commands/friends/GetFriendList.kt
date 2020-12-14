package com.twoilya.lonelyboardgamer.actions.commands.friends

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.actions.commands.batch
import com.twoilya.lonelyboardgamer.tables.FriendStatus
import com.twoilya.lonelyboardgamer.tables.ShortProfileInfo
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import io.ktor.http.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

object GetFriendList : TableCommand<List<ShortProfileInfo>>() {
    override fun query(userId: Long?, parameters: Parameters): List<ShortProfileInfo> {
        require(userId != null)
        { "Get friend list is user specific operation but no user id provided" }

        return getRelationListQuery(userId) {
            ((UsersRelations.first eq userId) or (UsersRelations.second eq userId)) and
                    (UsersRelations.status eq FriendStatus.Friends)
        }.batch(limit = parameters["limit"]?.toInt(), offset = parameters["offset"]?.toInt())
    }
}