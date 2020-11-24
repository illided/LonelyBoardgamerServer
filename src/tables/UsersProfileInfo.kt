package com.twoilya.lonelyboardgamer.tables

import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UsersProfileInfo : Table() {
    private const val FIRST_NAME_MAX_LENGTH = 20
    private const val SECOND_NAME_MAX_LENGTH = 20
    private const val PREF_CATEGORIES_MAX_LENGTH = 50
    private const val PREF_MECHANICS_MAX_LENGTH = 50
    private const val DESCRIPTION_MAX_LENGTH = 250

    val id: Column<String> = varchar("id", 10)
    val firstName: Column<String> = varchar("firstName", FIRST_NAME_MAX_LENGTH)
    val secondName: Column<String> = varchar("secondName", SECOND_NAME_MAX_LENGTH)
    val description: Column<String?> = varchar("description", DESCRIPTION_MAX_LENGTH).nullable()

    //must be changed
    val address: Column<String> = varchar("address", 100)
    val prefCategories: Column<String?> = varchar("prefCategories", PREF_CATEGORIES_MAX_LENGTH).nullable()
    val prefMechanics: Column<String?> = varchar("prefMechanics", PREF_MECHANICS_MAX_LENGTH).nullable()

    override val primaryKey = PrimaryKey(id)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProfileInfo (
    val id: String,
    val firstName: String,
    val secondName: String,
    val address: String? = null,
    val prefCategories: List<String>,
    val prefMechanics: List<String>,
    val description: String
)
