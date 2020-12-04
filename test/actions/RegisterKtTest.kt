package actions

import TestParameters
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.InfoMissingException
import com.twoilya.lonelyboardgamer.actions.commands.register.AddUser
import com.twoilya.lonelyboardgamer.auth.isExist
import com.twoilya.lonelyboardgamer.geo.Geocoder
import com.twoilya.lonelyboardgamer.tables.*
import com.twoilya.lonelyboardgamer.vk.VKConnector
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import javax.sql.DataSource

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class RegisterKtTest {

    @Test
    @Order(1)
    fun `User created when all info provided`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Peterburg"
        myParams["description"] = "I love cheese"
        myParams["prefCategories"] = "Test 1,Test 2"
        myParams["prefMechanics"] = "Test 3,Test 4"

        runBlocking { AddUser.execute("0", myParams) }

        assertTrue(runBlocking { isExist("0") })
    }

    @Test
    fun `No additional info provided but user still added`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Moscow"

        runBlocking { AddUser.execute("1", myParams) }

        assertTrue(runBlocking {isExist("1") })
    }

    @Test
    fun `User not created when necessary info not provided`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"

        assertThrows(InfoMissingException::class.java) {
            runBlocking { AddUser.execute("2", myParams) }
        }
    }

    @Test
    fun `Exception thrown when not all categories found`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Moscow"
        myParams["prefCategories"] = "Test 6,Test 2"

        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { AddUser.execute("2", myParams) }
        }
    }

    @Test
    fun `Exception thrown when not all mechanics found`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Moscow"
        myParams["prefMechanics"] = "Test 6,Test 2"

        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { AddUser.execute("2", myParams) }
        }
    }

    @Test
    fun `Exception thrown when duplicate of categories presented`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Moscow"
        myParams["prefCategories"] = "Test 5,Test 5"

        assertThrows(BadDataException::class.java) {
            runBlocking { AddUser.execute("2", myParams) }
        }
    }

    @Test
    fun `Exception thrown when duplicate of mechanics presented`() {
        val myParams = TestParameters()
        myParams["VKAccessToken"] = "token"
        myParams["address"] = "Moscow"
        myParams["prefMechanics"] = "Test 5,Test 5"

        assertThrows(BadDataException::class.java) {
            runBlocking { AddUser.execute("2", myParams) }
        }
    }

    companion object {
        private val embeddedPostgres = EmbeddedPostgres.start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            Database.connect(dataSource)

            val testList = listOf(
                "1" to "Test 1",
                "2" to "Test 2",
                "3" to "Test 3",
                "4" to "Test 4",
                "5" to "Test 5"
             )

            transaction {
                SchemaUtils.create(
                    UsersLoginInfo,
                    UsersProfileInfo,
                    BGCategories,
                    BGMechanics,
                    UsersLocations
                )

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

            mockkObject(VKConnector)
            every { VKConnector.getName(any()) } returns ("Ivan" to "Ivanov")

            mockkObject(Geocoder)
            every { Geocoder.getCoordinates(any()) } returns ("50.0" to "50.0")
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            embeddedPostgres.close()
            unmockkAll()
        }
    }
}