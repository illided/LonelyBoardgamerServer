package actions

import TestParameters
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import com.twoilya.lonelyboardgamer.actions.commands.profile.ChangeDescription
import com.twoilya.lonelyboardgamer.actions.commands.profile.GetPersonalData
import com.twoilya.lonelyboardgamer.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class ProfileKtTest {

    @Test
    fun `Profile data received when user exists`() {
        val gotFromDB = runBlocking { GetPersonalData.execute("0") }
        val expected = ProfileInfo(
            id = "0",
            firstName = "John",
            secondName = "Wick",
            description = "I love my dog",
            address = "Great Britain",
            prefMechanics = emptyList(),
            prefCategories = emptyList()
        )
        assertEquals(expected, gotFromDB)
    }

    @Test
    fun `Exception thrown when tries to get data about non existing user`() {
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { GetPersonalData.execute("1") }
        }
    }

    @Test
    fun `Description changed when user exists and params correct`() {
        val params = TestParameters()
        params["new"] = "They took my car"
        runBlocking { ChangeDescription.execute("0", params) }
        assertEquals(
            "They took my car",
            transaction {
                UsersProfileInfo
                    .select { UsersProfileInfo.id eq "0" }
                    .map { it[UsersProfileInfo.description].toString() }
            }.component1()
        )
    }

    @Test
    fun `Exception thrown when description too big`() {
        val params = TestParameters()
        params["new"] = "It is true that I have sent six bullets " +
                "through the head of my best friend, and yet I hope " +
                "to shew by this statement that I am not his murderer." +
                " At first I shall be called a madman—madder than the " +
                "man I shot in his cell at the Arkham Sanitarium. Later" +
                " some of my readers will weigh each statement, correlate" +
                " it with the known facts, and ask themselves how I could" +
                " have believed otherwise than as I did after facing the" +
                " evidence of that horror—that thing on the doorstep."

        assertThrows(WrongDataFormatException::class.java) {
            runBlocking { ChangeDescription.execute("0", params) }
        }
    }

    companion object {
        private val embeddedPostgres = EmbeddedPostgres.start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            Database.connect(dataSource)
            transaction {
                create(
                    UsersLoginInfo,
                    UsersProfileInfo,
                    BGCategories,
                    BGMechanics,
                    UsersLocations
                )
                UsersProfileInfo.insert {
                    it[id] = "0"
                    it[firstName] = "John"
                    it[secondName] = "Wick"
                    it[description] = "I love my dog"
                    it[address] = "Great Britain"
                    it[prefMechanics] = ""
                    it[prefCategories] = ""
                }
                UsersLoginInfo.insert {
                    it[id] = "0"
                    it[lastLogout] = DateTime(System.currentTimeMillis())
                }
                UsersLocations.insert {
                    it[id] = "0"
                    it[latitude] = 50.0
                    it[longitude] = 50.0
                }
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            embeddedPostgres.close()
        }
    }
}
