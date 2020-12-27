package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.or

object UsersRelations : Table() {
    val first = reference("person1", UsersProfileInfo.id)
    val second = reference("person2", UsersProfileInfo.id).check { it greater first }
    val status = customEnumeration(
        "status",
        "friendStatus",
        { value -> FriendStatus.valueOf(value as String) },
        { PGEnum("FriendStatus", it) })
    val actionUser = reference("actionUser", UsersProfileInfo.id).check {(it eq first) or (it eq second)}

    override val primaryKey: PrimaryKey = PrimaryKey(first, second)
}

enum class FriendStatus() {
    Pending,
    Hidden,
    Friends
}