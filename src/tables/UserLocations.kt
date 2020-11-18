package com.twoilya.lonelyboardgamer.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UserLocations : Table() {
    val id : Column<String> = reference("id", UsersProfileInfo.id)
    val latitude: Column<Double> = double("lat")
    val longitude: Column<Double> = double("lng")

    override val primaryKey = PrimaryKey(UserLocations.id)
}