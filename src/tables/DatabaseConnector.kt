package com.twoilya.lonelyboardgamer.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

object DatabaseConnector {
    fun init() {
        Database.connect(hikari())
        transaction {
            create(UserLoginInfo, UserProfileInfo)
        }
    }

    private fun hikari(): HikariDataSource {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }
        println(dotenv["MY_SECRET_KEY"])
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dotenv["JDBC_DATABASE_URL"]
        config.username = dotenv["JDBC_DATABASE_USERNAME"]
        config.password = dotenv["JDBC_DATABASE_PASSWORD"]
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }
}