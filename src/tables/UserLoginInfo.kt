package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object UserLoginInfo : Table() {
    val id: Column<String> = varchar("id", 10)
    val lastLogout: Column<DateTime> = datetime("lastLogout")

    override val primaryKey = PrimaryKey(id)
}