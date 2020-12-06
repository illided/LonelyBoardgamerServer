package com.twoilya.lonelyboardgamer.auth

import com.twoilya.lonelyboardgamer.tables.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.util.*

private val mapper: (ResultRow) -> Credentials = {
    Credentials(
        id = it[UsersLoginInfo.id],
        lastLogout = it[UsersLoginInfo.lastLogout]
    )
}

fun getIdQuery(VKid: String) : Long? =
    UsersProfileInfo.select{UsersProfileInfo.VKid eq VKid }
        .map {it[UsersProfileInfo.id]}
        .also { if (it.isEmpty()) return null }
        .component1()

suspend fun isExistWithSuchVKid(VKid: String) = dbQuery { getIdQuery(VKid) } != null

suspend fun isUserLoggedIn(userId: Long, iat: Date): Boolean {
    val user = dbQuery {  UsersLoginInfo.findInTable(userId, UsersLoginInfo.id, mapper) }
    return !(user == null || user.lastLogout.isAfter(DateTime(iat)))
}
