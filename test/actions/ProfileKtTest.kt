package actions

import com.twoilya.lonelyboardgamer.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class ProfileKtTest {

    @Test
    fun test() {
        transaction {
            // DB requests
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
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            embeddedPostgres.close()
        }
    }
}
