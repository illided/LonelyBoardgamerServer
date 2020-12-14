package com.twoilya.lonelyboardgamer.tables

import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UsersProfileInfo : Table() {
    private const val FIRST_NAME_MAX_LENGTH = 20
    private const val SECOND_NAME_MAX_LENGTH = 20
    private const val PREF_CATEGORIES_MAX_LENGTH = 250
    private const val PREF_MECHANICS_MAX_LENGTH = 250
    const val DESCRIPTION_MAX_LENGTH = 250

    val id: Column<Long> = long("id").autoIncrement().index()
    val VKid: Column<String> = varchar("VKid", 20).uniqueIndex()
    val firstName: Column<String> = varchar("firstName", FIRST_NAME_MAX_LENGTH)
    val secondName: Column<String> = varchar("secondName", SECOND_NAME_MAX_LENGTH)
    val description: Column<String?> = varchar("description", DESCRIPTION_MAX_LENGTH).nullable()

    //must be changed
    val address: Column<String> = varchar("address", 100)
    val prefCategories: Column<String?> = varchar("prefCategories", PREF_CATEGORIES_MAX_LENGTH).nullable()
    val prefMechanics: Column<String?> = varchar("prefMechanics", PREF_MECHANICS_MAX_LENGTH).nullable()

    override val primaryKey = PrimaryKey(id)
}

data class PersonalProfileInfo(
    val firstName: String,
    val secondName: String,
    val address: String,
    val prefCategories: List<String>,
    val prefMechanics: List<String>,
    val description: String
)

data class ShortProfileInfo(
    val id: Long,
    val firstName: String,
    val secondName: String,
)