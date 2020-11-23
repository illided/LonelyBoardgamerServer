package com.twoilya.lonelyboardgamer.tables

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.twoilya.lonelyboardgamer.geo.getDistance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.IOException
import kotlin.math.min
import kotlin.math.round

object UsersLocations : Table() {
    val id: Column<String> = reference("id", UsersProfileInfo.id)
    val latitude: Column<Double> = double("lat")
    val longitude: Column<Double> = double("lng")

    override val primaryKey = PrimaryKey(UsersLocations.id)

    fun findNearest(userId: String, limit: Int?, offset: Int?): List<DistanceCredentials> {
        return transaction {
            val userLocation = UsersLocations
                .select { UsersLocations.id eq userId }
                .limit(1)
                .map { Pair(it[latitude].toDouble(), it[longitude].toDouble()) }
                .component1()


            val nearestPeopleQuery = UsersLocations.join(UsersProfileInfo, JoinType.INNER)
                .slice(
                    UsersProfileInfo.id,
                    UsersProfileInfo.firstName,
                    UsersProfileInfo.secondName,
                    UsersLocations.latitude,
                    UsersLocations.longitude
                )
                .select {
                    latitude.between(userLocation.first - 0.1, userLocation.first + 0.1) and
                            longitude.between(userLocation.second - 0.1, userLocation.second + 0.1) and
                            (UsersProfileInfo.id neq userId)
                }

            val nearestPeopleDistances = nearestPeopleQuery.map {
                DistanceCredentials(
                    it[UsersProfileInfo.id],
                    it[UsersProfileInfo.firstName],
                    it[UsersProfileInfo.secondName],
                    getDistance(userLocation, (it[latitude] to it[longitude]))
                )
            }

            val lowerBound = offset ?: 0
            val upperBound = lowerBound + (limit ?: nearestPeopleDistances.size)

            return@transaction nearestPeopleDistances.sortedBy { it.distance }.subList(
                min(lowerBound, nearestPeopleDistances.size - 1),
                min(upperBound, nearestPeopleDistances.size - 1)
            )
        }
    }
}

data class DistanceCredentials(
    val id: String,
    val firstName: String,
    val secondName: String,
    @JsonSerialize(using = PrettyDistanceSerializer::class)
    val distance: Double
)

class PrettyDistanceSerializer: JsonSerializer<Double>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: Double, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeObject(if (value > 1.0) {"$value км"} else {"${value * 1000} м"})
    }
}