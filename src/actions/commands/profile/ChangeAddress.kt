package com.twoilya.lonelyboardgamer.actions.commands.profile

import actions.commands.TableCommand
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.tables.UsersLocations
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.update

object ChangeAddress : TableCommand<Unit>() {
    override fun query(userId: Long?, parameters: Parameters) {
        require(userId != null) { "Change address is user specific but no user id provided" }

        val new = parameters["new"] ?: throw InfoMissingException(
            "No address provided"
        )
        UsersProfileInfo.update({ UsersProfileInfo.id eq userId }) {
            it[address] = new
        }

        val newCoordinates = Geocoder.getCoordinates(new)

        UsersLocations.update({ UsersLocations.id eq userId }) {
            it[latitude] = newCoordinates.first.toDouble()
            it[longitude] = newCoordinates.second.toDouble()
        }
    }
}