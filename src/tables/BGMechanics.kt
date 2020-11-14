package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BGMechanics : Table() {
    val id: Column<String> = varchar("id", 3)
    val name: Column<String> = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name)

    fun findById(ids: List<String>?): List<String> {
        if (ids == null) {
            return emptyList()
        }
        return transaction {
            BGMechanics.select { BGMechanics.id inList ids }.map { it[name].toString() }
        }
    }

    fun findByName(names: List<String>?): List<String> {
        if (names == null) {
            return emptyList()
        }

        return transaction {
            BGMechanics.select { name inList names }.map { it[BGMechanics.id] }
        }.also { if (it.size != names.size) throw IllegalArgumentException("Not all mechanics were founded") }
    }
}