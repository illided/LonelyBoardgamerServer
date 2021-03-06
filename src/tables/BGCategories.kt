package com.twoilya.lonelyboardgamer.tables

import com.twoilya.lonelyboardgamer.BadDataException
import com.twoilya.lonelyboardgamer.ElementWasNotFoundException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BGCategories : Table() {
    val id: Column<String> = varchar("id", 3)
    val name: Column<String> = varchar("name", 50)

    override val primaryKey = PrimaryKey(name)

    fun findById(ids: List<String>?): List<String> {
        if (ids == null) {
            return emptyList()
        }
        return transaction {
            BGCategories.select { BGCategories.id inList ids }.map { it[name].toString() }
        }
    }

    fun findByName(names: List<String>?): List<String> {
        if (names == null || names == listOf("")) {
            return emptyList()
        }

        if (names.toSet().size != names.size)
            throw BadDataException("Duplicate of categories presented")

        return transaction {
            BGCategories.select { BGCategories.name inList names }.map { it[BGCategories.id] }
        }.also {
            if (it.size != names.size) throw ElementWasNotFoundException("Not all categories were founded")
        }
    }
}
