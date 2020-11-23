package com.twoilya.lonelyboardgamer.tables

import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

object UserUtils {
    suspend fun addUser(userId: String, parameters: Parameters) {
        val userAddress = parameters["address"] ?: throw InfoMissingException("No address provided")
        val (vkFirstName, vkSecondName) = VKConnector.getName(userId)
        val (lat, lng) = Geocoder.getCoordinates(userAddress)

        transaction {
            UsersLoginInfo.insert {
                it[id] = userId
                it[lastLogout] = DateTime(System.currentTimeMillis()).secondOfDay().roundFloorCopy()
            }
            UsersProfileInfo.insert {
                it[id] = userId

                it[firstName] = vkFirstName
                it[secondName] = vkSecondName

                it[address] = userAddress

                it[description] = parameters["description"]

                it[prefCategories] = BGCategories.findByName(
                    parameters["prefCategories"]?.split(",")
                ).joinToString(",")

                it[prefMechanics] = BGMechanics.findByName(
                    parameters["prefMechanics"]?.split(",")
                ).joinToString(",")
            }
            UsersLocations.insert {
                it[id] = userId

                it[latitude] = lat.toDouble()
                it[longitude] = lng.toDouble()
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
