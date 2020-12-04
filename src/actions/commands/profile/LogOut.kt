package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import com.twoilya.lonelyboardgamer.tables.dbQuery
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

object LogOut : TableCommand<Unit>() {
    override fun query(userId: String, parameters: Parameters) {
        UsersLoginInfo.update({ UsersLoginInfo.id eq userId }) {
            it[lastLogout] = DateTime(System.currentTimeMillis())
        }
    }
}