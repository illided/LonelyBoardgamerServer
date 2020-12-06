package com.twoilya.lonelyboardgamer.tables

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }

fun <T, E> Table.findInTable(userId: E, idColumn: Column<E>, mapper: (ResultRow) -> T): T? {
    val searchResult = select { idColumn eq userId }
        .limit(1)
        .map { mapper(it) }
    return if (searchResult.size == 1) searchResult[0] else null
}