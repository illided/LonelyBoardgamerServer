package com.twoilya.lonelyboardgamer.tables

import com.twoilya.lonelyboardgamer.auth.AuthenticationException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.util.*

object UserUtils {
    fun isExist(userId: String) = getUserCredentials(userId) != null

    fun isUserLoggedIn(userId: String, iat: Date): Boolean {
        val user = getUserCredentials(userId)
        return !(user == null || user.lastLogout.isAfter(DateTime(iat)))
    }

    fun getUserCredentials(userId: String): Credentials? {
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

    fun getUserProfileInfo(userId: String): ProfileInfo? {
        val users = transaction {
            UsersProfileInfo.select { UsersProfileInfo.id eq userId }
                .limit(1)
                .map {
                    ProfileInfo(
                        id = it[UsersProfileInfo.id],
                        firstName = it[UsersProfileInfo.firstName],
                        secondName = it[UsersProfileInfo.secondName],
                        address = it[UsersProfileInfo.address]
                    )
                }
        }
        return if (users.isEmpty()) {
            null
        } else {
            users[0]
        }
    }

    fun addUser(userId: String, userFirstName: String, userSecondName: String, userAddress: String) {
        transaction {
            UsersLoginInfo.insert {
                it[id] = userId
                it[lastLogout] = DateTime(System.currentTimeMillis()).secondOfDay().roundFloorCopy()
            }
            UsersProfileInfo.insert {
                it[id] = userId
                it[address] = userAddress
                it[firstName] = userFirstName
                it[secondName] = userSecondName
            }
        }
    }

    fun logOut(userId: String) {
        transaction {
            UsersLoginInfo.update({UsersLoginInfo.id eq userId}) {
                it[UsersLoginInfo.lastLogout] = DateTime(System.currentTimeMillis())
            }
        }
    }
}
