package actions

import TestParameters
import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import com.twoilya.lonelyboardgamer.actions.commands.friends.*
import com.twoilya.lonelyboardgamer.tables.*
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.lang.Thread.sleep
import java.sql.SQLException
import javax.sql.DataSource

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class FriendsKtTest {
    private fun checkRelationRow(
        first: Long,
        second: Long,
        status: FriendStatus,
        actionUser: Long,
        isExists: Boolean = true
    ) {
        assertEquals(
            if (isExists) 1 else 0,
            transaction {
                UsersRelations.select {
                    (UsersRelations.first eq first) and
                            (UsersRelations.second eq second) and
                            (UsersRelations.status eq status) and
                            (UsersRelations.actionUser eq actionUser)
                }.map { 1 }.sum()
            }
        )
    }

    @Test
    @Order(1)
    fun `Pending relation created when request sent`() {
        val testParams = TestParameters()
        testParams["targetId"] = "5"
        runBlocking { SendRequest.run(1, testParams) }
        checkRelationRow(1, 5, FriendStatus.Pending, 1)
    }

    @Test
    @Order(2)
    fun `Exception thrown when tries to send request to non existing user`() {
        val testParams = TestParameters()
        testParams["targetId"] = "10"
        assertThrows(BadDataException::class.java) {
            runBlocking { SendRequest.run(1, testParams) }
        }
    }

    @Test
    @Order(3)
    fun `Exception thrown when tries to send another request to same user`() {
        val testParams = TestParameters()
        testParams["targetId"] = "5"
        runBlocking { SendRequest.run(2, testParams) }
        assertThrows(BadDataException::class.java) {
            runBlocking { SendRequest.run(2, testParams) }
        }
    }

    @Test
    @Order(4)
    fun `Status stays same when tries to send another request to same user`() {
        transaction {
            UsersRelations.insert {
                it[first] = 2
                it[second] = 4
                it[status] = FriendStatus.Friends
                it[actionUser] = 2
            }
        }

        val testParams = TestParameters()
        testParams["targetId"] = "4"
        try {
            runBlocking { SendRequest.run(2, testParams) }
        } catch (exception: BadDataException) {
        }
        checkRelationRow(2, 4, FriendStatus.Friends, 2)
    }

    @Test
    @Order(5)
    fun `Users are friends when request accepted`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Pending
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "1", "with" to "1")
        runBlocking { AnswerRequest.run(2, myParameters) }
        checkRelationRow(1, 2, FriendStatus.Friends, 1)
    }

    @Test
    @Order(6)
    fun `User hidden when request hidden`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Pending
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "1", "with" to "0")
        runBlocking { AnswerRequest.run(2, myParameters) }
        checkRelationRow(1, 2, FriendStatus.Hidden, 1)
    }

    @Test
    @Order(7)
    fun `Exception is thrown when answering on non existing request`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Pending
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "3", "with" to "1")
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { AnswerRequest.run(2, myParameters) }
        }
    }

    @Test
    @Order(7)
    fun `Exception is thrown when a response to your own request occurs`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Pending
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "2", "with" to "1")
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { AnswerRequest.run(1, myParameters) }
        }
    }

    @Test
    fun `Request revoked when trying to revoke existing request`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Pending
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "2")
        runBlocking { RevokeRequest.run(1, myParameters) }
        checkRelationRow(1, 2, FriendStatus.Pending, 1, false)
    }

    @Test
    fun `Exception is thrown when trying to revoke non existing request`() {
        val myParameters = TestParameters("targetId" to "10")
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { RevokeRequest.run(1, myParameters) }
        }
    }

    @Test
    fun `Exception is thrown when revoking as friends`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Friends
                it[actionUser] = 1
            }
        }

        val myParameters = TestParameters("targetId" to "2")
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { RevokeRequest.run(1, myParameters) }
        }
    }

    @Test
    fun `Request hidden and action user changed when deleting user from friend`() {
        transaction {
            UsersRelations.insert {
                it[first] = 1
                it[second] = 2
                it[status] = FriendStatus.Friends
                it[actionUser] = 1
            }
        }
        val myParameters = TestParameters("targetId" to "2")
        runBlocking { DeleteFromFriends.run(1, myParameters) }
        checkRelationRow(1, 2, FriendStatus.Hidden, 2)
    }

    @Test
    fun `Exception is thrown when deleting non friendly user`() {
        val myParameters = TestParameters("targetId" to "2")
        assertThrows(ElementWasNotFoundException::class.java) {
            runBlocking { DeleteFromFriends.run(1, myParameters) }
        }
    }

    private fun relationsSetup(relations: List<Pair<FriendStatus, List<Long>>>){
        transaction {
            UsersRelations.batchInsert(relations) {
                this[UsersRelations.status] = it.first
                this[UsersRelations.first] = it.second[0]
                this[UsersRelations.second] = it.second[1]
                this[UsersRelations.actionUser] = it.second[2]
            }
        }
    }

    @Test
    fun `Correct num of entries got when requesting friend lists`() {
        val relations = listOf<Pair<FriendStatus, List<Long>>>(
            FriendStatus.Friends to listOf(1,2,1),
            FriendStatus.Friends to listOf(1,3,1),
            FriendStatus.Hidden to listOf(1,4,1),
            FriendStatus.Pending to listOf(1, 5, 1)
        )
        relationsSetup(relations)
        assertEquals(2, runBlocking { GetFriendList.run(1,TestParameters()).size })
    }

    @Test
    fun `Correct num of entries got when requesting to me requests list`(){
        val relations = listOf<Pair<FriendStatus, List<Long>>>(
            FriendStatus.Friends to listOf(1,2,1),
            FriendStatus.Friends to listOf(1,3,1),
            FriendStatus.Hidden to listOf(1,4,4),
            FriendStatus.Pending to listOf(1, 5, 5)
        )
        relationsSetup(relations)
        assertEquals(1, runBlocking { GetToMeRequests.run(1,TestParameters()).size })
    }

    @Test
    fun `Correct num of entries got when requesting from me requests list`(){
        val relations = listOf<Pair<FriendStatus, List<Long>>>(
            FriendStatus.Friends to listOf(1,2,1),
            FriendStatus.Friends to listOf(1,3,1),
            FriendStatus.Hidden to listOf(1,4,1),
            FriendStatus.Pending to listOf(1, 5, 1)
        )
        relationsSetup(relations)
        assertEquals(2, runBlocking { GetFromMeRequests.run(1,TestParameters()).size })
    }

    @AfterEach
    fun clearRelations() {
        transaction { UsersRelations.deleteAll() }
    }

    companion object {
        private val embeddedPostgres = EmbeddedPostgres.start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            DatabaseConnector.init(dataSource)

            val mockedCatsAndMecs = listOf(
                "1" to "Test 1",
                "2" to "Test 2",
                "3" to "Test 3",
                "4" to "Test 4",
                "5" to "Test 5"
            )

            val userInfoEntries = listOf(
                "1" to listOf("John", "Wick", "I love my dog", "Great Britain", "1,2", "2,3"),
                "2" to listOf("Ivan", "Ivanov", "So lonely...", "Ural", "2,3", "4,5"),
                "3" to listOf("Svetlana", "Svetova", "Lets play munchkin", "Moscow", "1,3", "2,5"),
                "4" to listOf("Michael", "Michailov", "I love monopoly", "Ekaterinburg", "1", "5"),
                "5" to listOf("Anton", "Anton Antonov", "I wish i had party for gloomhaven", "Peterburg", "2", "3")
            )

            transaction {
                UsersProfileInfo.batchInsert(
                    userInfoEntries
                ) { entry ->
                    val id = entry.first
                    val parameters = entry.second

                    this[UsersProfileInfo.VKid] = id
                    this[UsersProfileInfo.firstName] = parameters[0]
                    this[UsersProfileInfo.secondName] = parameters[1]
                    this[UsersProfileInfo.description] = parameters[2]
                    this[UsersProfileInfo.address] = parameters[3]
                    this[UsersProfileInfo.prefCategories] = parameters[4]
                    this[UsersProfileInfo.prefMechanics] = parameters[5]
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