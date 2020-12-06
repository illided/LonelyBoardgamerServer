package com.twoilya.lonelyboardgamer.actions.commands.register

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.AuthorizationException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.auth.getIdQuery
import com.twoilya.lonelyboardgamer.auth.isExistWithSuchVKid
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.tables.*
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.joda.time.DateTime
import java.lang.IllegalArgumentException

object AddUser : TableCommand<Long>() {
    override fun query(userId: Long?, parameters: Parameters) : Long {
        val userAddress = parameters["address"]
            ?: throw InfoMissingException("No address provided")
        val (lat, lng) = Geocoder.getCoordinates(userAddress)

        val vkAccessToken = parameters["VKAccessToken"]
            ?: throw InfoMissingException("No token provided")

        val vkId = VKConnector.checkToken(vkAccessToken)
        if (runBlocking { isExistWithSuchVKid(vkId) }) {
            throw AuthorizationException("Such user already exist")
        }
        val (vkFirstName, vkSecondName) = VKConnector.getName(vkId)

        UsersProfileInfo.insert {
            it[firstName] = vkFirstName
            it[secondName] = vkSecondName

            it[VKid] = vkId

            it[address] = userAddress

            it[description] = parameters["description"]

            it[prefCategories] = BGCategories.findByName(
                parameters["prefCategories"]?.split(",")
            ).joinToString(",")

            it[prefMechanics] = BGMechanics.findByName(
                parameters["prefMechanics"]?.split(",")
            ).joinToString(",")
        }

        val newUserId = getIdQuery(vkId)
            ?: throw IllegalArgumentException("User was not added when it should have been")

        UsersLoginInfo.insert {
            it[id] = newUserId
            it[lastLogout] = DateTime(System.currentTimeMillis()).secondOfDay().roundFloorCopy()
        }
        UsersLocations.insert {
            it[id] = newUserId
            it[latitude] = lat.toDouble()
            it[longitude] = lng.toDouble()
        }

        return newUserId
    }
}