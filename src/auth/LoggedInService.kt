package com.twoilya.lonelyboardgamer.auth

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.tables.Credentials
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.DateTime
import java.util.*

object LoggedInService : TableCommand() {
    private val mapper: (ResultRow) -> Credentials = {
        Credentials(
            id = it[UsersLoginInfo.id],
            lastLogout = it[UsersLoginInfo.lastLogout]
        )
    }

    suspend fun isExist(userId: String) = UsersLoginInfo.findInTable(userId, UsersLoginInfo.id, mapper) != null

    suspend fun isUserLoggedIn(userId: String, iat: Date): Boolean {
        val user = UsersLoginInfo.findInTable(userId, UsersLoginInfo.id, mapper)
        return !(user == null || user.lastLogout.isAfter(DateTime(iat)))
    }
}