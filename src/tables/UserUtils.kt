package com.twoilya.lonelyboardgamer.tables

import com.twoilya.lonelyboardgamer.auth.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.util.*

object UserUtils {
    fun isExist(userId: String) = findUserCredentials(userId) != null

    fun isUserLoggedIn(userId: String, iat: Date): Boolean {
        val user = findUserCredentials(userId)
        require(user?.id != null && user.last_logout != null) {"Not enough information in JWT token"}
        println("${DateTime(iat)} ${user?.last_logout}")
        return !(user == null || user.last_logout.isAfter(DateTime(iat)))
    }

    private fun findUserCredentials(userId: String): User? {
        val users = transaction {
            UserLoginInfo.select { UserLoginInfo.id eq userId }.limit(1)
                .map {
                    User(
                        id = it[UserLoginInfo.id],
                        last_logout = it[UserLoginInfo.lastLogout]
                    )
                }
        }
        return if (users.isEmpty()) {
            null
        } else {
            users[0]
        }
    }

    fun getUserProfileInfo(userId: String): User? {
        val users = transaction {
            UserProfileInfo.select { UserProfileInfo.id eq userId }
                .limit(1)
                .map {
                    User(
                        id = it[UserProfileInfo.id],
                        first_name = it[UserProfileInfo.firstName],
                        last_name = it[UserProfileInfo.secondName],
                        address = it[UserProfileInfo.address]
                    )
                }
        }
        return if (users.isEmpty()) {
            null
        } else {
            users[0]
        }
    }

    fun addUser(user: User) {
        require(
            user.address != null
                    && user.id != null
                    && user.first_name != null
                    && user.last_name != null
        ) { "Not all information provided" }

        transaction {
            UserLoginInfo.insert {
                it[id] = user.id
                it[lastLogout] = DateTime(System.currentTimeMillis())
            }
            UserProfileInfo.insert {
                it[id] = user.id
                it[address] = user.address
                it[firstName] = user.first_name
                it[secondName] = user.last_name
            }
        }
    }

    fun logOut(userId: String) {
        transaction {
            UserLoginInfo.update({UserLoginInfo.id eq userId}) {
                it[UserLoginInfo.lastLogout] = DateTime(System.currentTimeMillis())
            }
        }
    }
}