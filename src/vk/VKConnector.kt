package com.twoilya.lonelyboardgamer.vk

import com.fasterxml.jackson.databind.JsonMappingException
import com.twoilya.lonelyboardgamer.AuthorizationException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.runBlocking

object VKConnector {
    private const val CHECK_TOKEN = "https://api.vk.com/method/secure.checkToken/"
    private const val USERS_GET = "https://api.vk.com/method/users.get/"
    private const val VK_API_VERSION = "v=5.21"
    private const val IN_RUSSIAN = "lang=ru"
    private val SERVER_TOKEN = "access_token=" + dotenv { ignoreIfMissing = true }["VK_SERVER_ACCESS_TOKEN"]

    private val regexForToken = "[0-9a-z]+".toRegex()

    private suspend inline fun <reified T> connectAndGet(url: String, exception: AuthorizationException): T {
        return HttpClient() {
            install(JsonFeature) { serializer = JacksonSerializer() }
        }.use { client ->
            try {
                client.get { url(url) }
            } catch (e: JsonMappingException) {
                throw exception
            }
        }
    }

    fun checkToken(token: String): String {
        if (!token.matches(regexForToken)) {
            throw WrongDataFormatException("Invalid access token format")
        }

        return runBlocking {
            connectAndGet<CheckTokenResponse>(
                url = "$CHECK_TOKEN?token=$token&$SERVER_TOKEN&$VK_API_VERSION",
                exception = AuthorizationException("Invalid access token")
            ).response.user_id
        }
    }

    fun getName(vkId: String): Pair<String, String> {
        val response =
            runBlocking {
                connectAndGet<UsersGetResponse>(
                    url = "$USERS_GET?user_ids=$vkId&$IN_RUSSIAN&$SERVER_TOKEN&$VK_API_VERSION",
                    exception = AuthorizationException("Server token is invalid or such id does not exist")
                )
            }

        require(response.response.size == 1) { "No user with such id" }
        val user = response.response.component1()
        return Pair(user.first_name, user.last_name)
    }
}

