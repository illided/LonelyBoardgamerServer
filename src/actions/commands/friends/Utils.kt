package com.twoilya.lonelyboardgamer.actions.commands.friends

import com.twoilya.lonelyboardgamer.actions.commands.LIMIT_FOR_LIST_QUERIES
import com.twoilya.lonelyboardgamer.tables.ShortProfileInfo
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import com.twoilya.lonelyboardgamer.tables.UsersRelations
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select

fun getRelationListQuery(userId: Long, selectStatement: SqlExpressionBuilder.() -> Op<Boolean>): List<ShortProfileInfo> {
    val friendsIds = UsersRelations
        .select { selectStatement() }
        .limit(LIMIT_FOR_LIST_QUERIES)
        .map {
            if (it[UsersRelations.first] == userId) {
                it[UsersRelations.second].toLong()
            } else {
                it[UsersRelations.first].toLong()
            }
        }


    return UsersProfileInfo.slice(
        UsersProfileInfo.id,
        UsersProfileInfo.firstName,
        UsersProfileInfo.secondName
    )
        .select { UsersProfileInfo.id inList friendsIds }
        .map {
            ShortProfileInfo(
                id = it[UsersProfileInfo.id],
                firstName = it[UsersProfileInfo.firstName],
                secondName = it[UsersProfileInfo.secondName]
            )
        }
}