package com.twoilya.lonelyboardgamer.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException
import javax.sql.DataSource

object DatabaseConnector {
    fun init(dataSource: DataSource = hikari()) {
        Database.connect(dataSource)
        transaction {
            exec("DO $$ BEGIN " +
                            "CREATE TYPE FriendStatus AS ENUM ('Pending', 'Hidden', 'Friends'); " +
                       "EXCEPTION " +
                            "WHEN duplicate_object THEN null; " +
                       "END $$;")
            create(
                UsersLoginInfo,
                UsersProfileInfo,
                BGCategories,
                BGMechanics,
                UsersLocations,
                UsersRelations
            )
        }
    }

    private fun hikari(): HikariDataSource {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = dotenv["JDBC_DATABASE_URL"]
            username = dotenv["JDBC_DATABASE_USERNAME"]
            password = dotenv["JDBC_DATABASE_PASSWORD"]
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}