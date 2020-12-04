package com.twoilya.lonelyboardgamer.actions.commands.register

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.tables.*
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AddUser : TableCommand<Unit>() {
    override fun query(userId: String, parameters: Parameters) {
        val userAddress = parameters["address"] ?: throw InfoMissingException("No address provided")
        val (vkFirstName, vkSecondName) = VKConnector.getName(userId)
        val (lat, lng) = Geocoder.getCoordinates(userAddress)

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