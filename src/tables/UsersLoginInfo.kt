package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

object UsersLoginInfo : Table() {
    val id: Column<String> = varchar("id", 10)
    val lastLogout: Column<DateTime> = datetime("lastLogout")

    override val primaryKey = PrimaryKey(id)

    fun isExist(userId: String) = getUserCredentials(userId) != null

    fun isUserLoggedIn(userId: String, iat: Date): Boolean {
        val user = getUserCredentials(userId)
        return !(user == null || user.lastLogout.isAfter(DateTime(iat)))
    }

    private fun getUserCredentials(userId: String): Credentials? {
        val users = transaction {
            UsersLoginInfo.select { UsersLoginInfo.id eq userId }.limit(1)
                .map {
                    Credentials(
                        id = it[UsersLoginInfo.id],
                        lastLogout = it[UsersLoginInfo.lastLogout]
                    )
                }
        }
        return if (users.isEmpty()) {
            null
        } else {
            users[0]
        }
    }
}

data class Credentials(val id: String, val lastLogout: DateTime)