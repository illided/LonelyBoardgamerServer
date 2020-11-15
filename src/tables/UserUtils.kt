package com.twoilya.lonelyboardgamer.tables

import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.http.Parameters
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

    suspend fun addUser(userId: String, parameters: Parameters) {
        val (vkFirstName, vkSecondName) = VKConnector.getName(userId)
        transaction {
            UsersLoginInfo.insert {
                it[id] = userId
                it[lastLogout] = DateTime(System.currentTimeMillis()).secondOfDay().roundFloorCopy()
            }
            UsersProfileInfo.insert {
                it[id] = userId

                it[firstName] = vkFirstName
                it[secondName] = vkSecondName

                it[address] = parameters["address"] ?: throw InfoMissingException("No address provided")

                it[description] = parameters["description"]

                it[prefCategories] = BGCategories.findByName(
                    parameters["prefCategories"]?.split(",")
                ).joinToString(",")

                it[prefMechanics] = BGMechanics.findByName(
                    parameters["prefMechanics"]?.split(",")
                ).joinToString(",")
            }
        }
    }

    fun logOut(userId: String) {
        transaction {
            UsersLoginInfo.update({ UsersLoginInfo.id eq userId }) {
                it[UsersLoginInfo.lastLogout] = DateTime(System.currentTimeMillis())
            }
        }
    }
}
