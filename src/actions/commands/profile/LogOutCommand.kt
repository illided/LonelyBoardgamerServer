package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.tables.UsersLoginInfo
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

class LogOutCommand() : TableCommand() {
    suspend fun execute(userId: String) {
        dbQuery<Unit> {
            UsersLoginInfo.update({ UsersLoginInfo.id eq userId }) {
                it[lastLogout] = DateTime(System.currentTimeMillis())
            }
        }
    }
}