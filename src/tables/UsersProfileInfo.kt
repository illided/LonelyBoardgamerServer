package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UsersProfileInfo : Table() {
    object Lengths {
        const val FIRST_NAME_MAX_LENGTH = 20
        const val SECOND_NAME_MAX_LENGTH = 20
    }

    val id: Column<String> = varchar("id", 10)
    val firstName: Column<String> = varchar("firstName", Lengths.FIRST_NAME_MAX_LENGTH)
    val secondName: Column<String> = varchar("secondName", Lengths.SECOND_NAME_MAX_LENGTH)

    //must be changed
    val address: Column<String> = varchar("address", 100)

    override val primaryKey = PrimaryKey(id)
}

data class ProfileInfo(
    val id: String,
    val firstName: String,
    val secondName: String,
    val address: String
)