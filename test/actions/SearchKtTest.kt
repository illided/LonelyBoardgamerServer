package actions

import TestParameters
import actions.commands.search.SearchNearest
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.actions.commands.search.SearchPublicly
import com.twoilya.lonelyboardgamer.tables.*
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import javax.sql.DataSource

internal class SearchKtTest {

    @Test
    fun `Getting only people within small box when searching for nearest`() {
        assertEquals(
            3,
            runBlocking { SearchNearest.run("0", TestParameters()).size }
        )
    }

    @Test
    fun `Getting people with offset when searching with correct offset`() {
        val myParameters = TestParameters()
        myParameters["offset"] = "1"
        assertEquals(
            2,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Getting no more than limit when searching nearest with correct limit`() {
        val myParameters = TestParameters()
        myParameters["limit"] = "2"
        assertEquals(
            2,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Getting all when searching nearest with big limit`() {
        val myParameters = TestParameters()
        myParameters["limit"] = "100"
        assertEquals(
            3,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Getting none when searching nearest with small limit`() {
        val myParameters = TestParameters()
        myParameters["limit"] = "0"
        assertEquals(
            0,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Exception thrown when searching with negative limit`() {
        val myParameters = TestParameters()
        myParameters["limit"] = "-5"
        assertThrows(BadDataException::class.java) {
            runBlocking { SearchNearest.run("0", myParameters) }
        }
    }

    @Test
    fun `Getting none when searching nearest with big offset`() {
        val myParameters = TestParameters()
        myParameters["offset"] = "100"
        assertEquals(
            0,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Getting all when searching nearest with small offset`() {
        val myParameters = TestParameters()
        myParameters["offset"] = "0"
        assertEquals(
            3,
            runBlocking { SearchNearest.run("0", myParameters).size }
        )
    }

    @Test
    fun `Exception thrown when searching with negative offset`() {
        val myParameters = TestParameters()
        myParameters["offset"] = "-5"
        assertThrows(BadDataException::class.java) {
            runBlocking { SearchNearest.run("0", myParameters) }
        }
    }

    @Test
    fun `Getting correct profile info when searching by id and user exist`() {
        assertEquals(
            "Ivan",
            runBlocking { SearchPublicly.run("1") }?.firstName
        )
    }

    @Test
    fun `Getting null when searching by id and user does not exist`() {
        assertEquals(
            null,
            runBlocking { SearchPublicly.run("100") }
        )
    }

    companion object {
        private val embeddedPostgres = EmbeddedPostgres.start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            Database.connect(dataSource)

            val mockedCatsAndMecs = listOf(
                "1" to "Test 1",
                "2" to "Test 2",
                "3" to "Test 3",
                "4" to "Test 4",
                "5" to "Test 5"
            )

            val userInfoEntries = listOf(
                "0" to listOf("John", "Wick", "I love my dog", "Great Britain", "1,2", "2,3"),
                "1" to listOf("Ivan", "Ivanov", "So lonely...", "Ural", "2,3", "4,5"),
                "2" to listOf("Svetlana", "Svetova", "Lets play munchkin", "Moscow", "1,3", "2,5"),
                "3" to listOf("Michael", "Michailov", "I love monopoly", "Ekaterinburg", "1", "5"),
                "4" to listOf("Anton", "Anton Antonov", "I wish i had party for gloomhaven", "Peterburg", "2", "3")
            )

            val locationsEntries = listOf(
                "0" to listOf(50.0, 50.0),
                "1" to listOf(50.01, 50.01),
                "2" to listOf(50.02, 50.02),
                "3" to listOf(50.03, 50.03),
                "4" to listOf(70.0, 70.0)
            )


            transaction {
                SchemaUtils.create(
                    UsersLoginInfo,
                    UsersProfileInfo,
                    BGCategories,
                    BGMechanics,
                    UsersLocations
                )

                UsersProfileInfo.batchInsert(
                    userInfoEntries
                ) { entry ->
                    val id = entry.first
                    val parameters = entry.second

                    this[UsersProfileInfo.id] = id
                    this[UsersProfileInfo.firstName] = parameters[0]
                    this[UsersProfileInfo.secondName] = parameters[1]
                    this[UsersProfileInfo.description] = parameters[2]
                    this[UsersProfileInfo.address] = parameters[3]
                    this[UsersProfileInfo.prefCategories] = parameters[4]
                    this[UsersProfileInfo.prefMechanics] = parameters[5]
                }

                UsersLocations.batchInsert(
                    locationsEntries
                ) { entry ->
                    val id = entry.first
                    val coordinates = entry.second

                    this[UsersLocations.id] = id
                    this[UsersLocations.latitude] = coordinates[0]
                    this[UsersLocations.longitude] = coordinates[1]
                }

                BGCategories.batchInsert(
                    mockedCatsAndMecs
                ) { cat ->
                    this[BGCategories.id] = cat.first
                    this[BGCategories.name] = cat.second
                }
                BGMechanics.batchInsert(
                    mockedCatsAndMecs
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