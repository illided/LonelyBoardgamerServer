package com.twoilya.lonelyboardgamer.tables

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.jetbrains.exposed.sql.*
import java.io.IOException

object UsersLocations : Table() {
    val id: Column<String> = reference("id", UsersProfileInfo.id)
    val latitude: Column<Double> = double("lat")
    val longitude: Column<Double> = double("lng")

    override val primaryKey = PrimaryKey(UsersLocations.id)
}
