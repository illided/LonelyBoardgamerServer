package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

object UsersLoginInfo : Table() {
    val id: Column<Long> = reference("id", UsersProfileInfo.id)
    val lastLogout: Column<DateTime> = datetime("lastLogout")

    override val primaryKey = PrimaryKey(id)
}

data class Credentials(val id: Long, val lastLogout: DateTime)