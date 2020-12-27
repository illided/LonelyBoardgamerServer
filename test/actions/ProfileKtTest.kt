package actions

import TestParameters
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.WrongDataFormatException
import com.twoilya.lonelyboardgamer.actions.commands.profile.*
import com.twoilya.lonelyboardgamer.auth.isUserLoggedIn
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.tables.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.*
import javax.sql.DataSource

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ProfileKtTest {

    @Test
    @Order(1)
    fun `Profile data received when user exists`() {
        val gotFromDB = runBlocking { GetPersonalData.run(1) }
        val expected = PersonalProfileInfo(
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
    fun `Lat and lon changed when address changed`() {
        val parameters = TestParameters()
        parameters["new"] = "Russia"

        mockkObject(Geocoder)
        every { Geocoder.getCoordinates("Russia") } returns ("70.0" to "70.0")

        runBlocking { ChangeAddress.run(1, parameters) }
        assertEquals(
            "70.0,70.0",
            transaction {
                UsersLocations
                    .select { UsersLocations.id eq 1 }
                    .map { "${it[UsersLocations.latitude]},${it[UsersLocations.longitude]}" }
            }.component1()
        )

        unmockkAll()
    }

    @Test
    fun `Exception thrown when tries to get data about non existing user`() {
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { GetPersonalData.run(10) }
        }
    }

    @Test
    fun `Description changed when user exists and params correct`() {
        val params = TestParameters()
        params["new"] = "They took my car"
        runBlocking { ChangeDescription.run(1, params) }
        assertEquals(
            "They took my car",
            transaction {
                UsersProfileInfo
                    .select { UsersProfileInfo.id eq 1 }
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
            runBlocking { ChangeDescription.run(1, params) }
        }
    }

    @Test
    fun `Categories changed when input is correct`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Test 2"
        runBlocking { ChangeCategories.run(1, parameters) }
        assertEquals(
            "1,2",
            transaction {
                UsersProfileInfo
                    .select { UsersProfileInfo.id eq 1 }
                    .map { it[UsersProfileInfo.prefCategories].toString() }
            }.component1()
        )
    }

    @Test
    fun `Categories changed when input is correct and have all categories`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Test 2,Test 3,Test 4"
        runBlocking { ChangeCategories.run(1, parameters) }
        assertEquals(
            "1,2,3,4",
            transaction {
                UsersProfileInfo
                    .select { UsersProfileInfo.id eq 1 }
                    .map { it[UsersProfileInfo.prefCategories].toString() }
            }.component1()
        )
    }

    @Test
    fun `Exception thrown when try to change pref cats not all categories found`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Bad cat"
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { ChangeCategories.run(1, parameters) }
        }
    }

    @Test
    fun `Exception thrown when try to change pref mecs and not all mechanics found`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Bad mec"
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { ChangeMechanics.run(1, parameters) }
        }
    }

    @Test
    fun `Exception thrown when duplicate of categories presented`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Duplicate cat,Duplicate cat"
        assertThrows(BadDataException::class.java) {
            runBlocking { ChangeCategories.run(1, parameters) }
        }
    }

    @Test
    fun `Exception thrown when duplicate of mechanics presented`() {
        val parameters = TestParameters()
        parameters["new"] = "Test 1,Duplicate mec,Duplicate mec"
        assertThrows(BadDataException::class.java) {
            runBlocking { ChangeMechanics.run(1, parameters) }
        }
    }

    @Test
    fun `User not logged in when he logged out`() {
        val iat = Date.from(Instant.now())
        runBlocking { LogOut.run(1) }
        assertFalse(runBlocking { isUserLoggedIn(1, iat) })
    }

    companion object {
        private val embeddedPostgres = EmbeddedPostgres.start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            DatabaseConnector.init(dataSource)

            val testList = listOf(
                "1" to "Test 1",
                "2" to "Test 2",
                "3" to "Test 3",
                "4" to "Test 4"
            )

            transaction {
                UsersProfileInfo.insert {
                    it[firstName] = "John"
                    it[secondName] = "Wick"
                    it[VKid] = "0"
                    it[description] = "I love my dog"
                    it[address] = "Great Britain"
                    it[prefMechanics] = ""
                    it[prefCategories] = ""
                }
                UsersLoginInfo.insert {
                    it[id] = 1
                    it[lastLogout] = DateTime(System.currentTimeMillis())
                }
                UsersLocations.insert {
                    it[id] = 1
                    it[latitude] = 50.0
                    it[longitude] = 50.0
                }
                BGCategories.batchInsert(
                    testList
                ) { cat ->
                    this[BGCategories.id] = cat.first
                    this[BGCategories.name] = cat.second
                }
                BGMechanics.batchInsert(
                    testList
                ) { mec ->
                    this[BGMechanics.id] = mec.first
                    this[BGMechanics.name] = mec.second
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
