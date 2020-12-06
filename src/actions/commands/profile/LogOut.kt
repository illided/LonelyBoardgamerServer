package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import io.ktor.http.*
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

object LogOut : TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        require(userId != null)
        { "Log out is user specific but no user id provided" }

        UsersLoginInfo.update({ UsersLoginInfo.id eq userId }) {
            it[lastLogout] = DateTime(System.currentTimeMillis())
        }
    }
}