package com.twoilya.lonelyboardgamer.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.runBlocking

object Geocoder {
    private val addressRegex = "[А-Яа-я. 0-9,-]+".toRegex()
    private const val tomtomAddressForGeocoding = "https://api.tomtom.com/search/2/geocode/"
    private const val country = "RU"
    private const val responseType = "json"
    private val apiKey = dotenv { ignoreIfMissing = true }["TOMTOM_API_KEY"]

    fun getCoordinates(address: String): Pair<String, String> {
        if (!address.matches(addressRegex)) {
            throw WrongDataFormatException("Wrong address format")
        }

        val response = runBlocking {
            HttpClient().use { client ->
                client.get<String> {
                    url(
                        tomtomAddressForGeocoding +
                                address.encodeURLParameter() +
                                "." + responseType +
                                "?limit=1" +
                                "&countrySet=" + country +
                                "&key=" + apiKey

                    )
                }
            }
        }
        val coordinates = ObjectMapper().readTree(response).at("/results/0/position")
        val lat = coordinates.path("lat").asText()
        val lng = coordinates.path("lon").asText()
        return lat to lng
    }
}