package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BGMechanics : Table() {
    val id: Column<Int> = integer("id")
    val name: Column<String> = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name)

    fun find(ids: List<Int>?): List<String> {
        if (ids == null) {
            return emptyList()
        }
        return transaction {
            BGMechanics.select { BGMechanics.id inList ids}.map { it[name].toString() }
        }
    }
}