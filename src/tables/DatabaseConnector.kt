package com.twoilya.lonelyboardgamer.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnector {
    fun init() {
        Database.connect(hikari())
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

    private fun hikari(): HikariDataSource {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }
        val config = HikariConfig().apply{
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