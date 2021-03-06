package actions.commands.search

import actions.commands.TableCommand
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.twoilya.lonelyboardgamer.actions.commands.batch
import com.twoilya.lonelyboardgamer.geo.getDistance
import com.twoilya.lonelyboardgamer.tables.UsersLocations
import com.twoilya.lonelyboardgamer.tables.UsersProfileInfo
import io.ktor.http.Parameters
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.io.IOException

object SearchNearest : TableCommand<List<SearchNearest.DistanceCredentials>>() {
    private const val BOX_LENGTH = 0.1

    override fun query(userId: Long?, parameters: Parameters): List<DistanceCredentials> {
        require(userId != null)
        {"Search nearest is user specific but no user id provided"}
        return findNearest(
            userId,
            parameters["limit"]?.toInt(),
            parameters["offset"]?.toInt()
        )
    }

    private fun findNearest(userId: Long, limit: Int?, offset: Int?): List<DistanceCredentials> {
        val userLocation = UsersLocations
            .select { UsersLocations.id eq userId }
            .limit(1)
            .map { Pair(it[UsersLocations.latitude].toDouble(), it[UsersLocations.longitude].toDouble()) }
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
                UsersLocations.latitude.between(
                    userLocation.first - BOX_LENGTH,
                    userLocation.first + BOX_LENGTH
                ) and
                        UsersLocations.longitude.between(
                            userLocation.second - BOX_LENGTH,
                            userLocation.second + BOX_LENGTH
                        ) and
                        (UsersProfileInfo.id neq userId)
            }

        val nearestPeopleDistances = nearestPeopleQuery.map {
            DistanceCredentials(
                it[UsersProfileInfo.id],
                it[UsersProfileInfo.firstName],
                it[UsersProfileInfo.secondName],
                getDistance(userLocation, (it[UsersLocations.latitude] to it[UsersLocations.longitude]))
            )
        }

        return nearestPeopleDistances.sortedBy { it.distance }.batch(offset, limit)
    }

    data class DistanceCredentials(
        val id: Long,
        val firstName: String,
        val secondName: String,
        @JsonSerialize(using = PrettyDistanceSerializer::class)
        val distance: Double
    )

    class PrettyDistanceSerializer : JsonSerializer<Double>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: Double, gen: JsonGenerator?, serializers: SerializerProvider?) {
            gen?.writeObject(
                if (value > 1.0) {
                    "$value км"
                } else {
                    "${(value * 1000).toInt()} м"
                }
            )
        }
    }
}