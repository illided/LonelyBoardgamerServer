package com.twoilya.lonelyboardgamer.geo

import kotlin.math.*

private const val EARTH_RADIUS = 6371

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun getDistance(firstPoint: Pair<Double, Double>, secondPoint: Pair<Double, Double>): Double {
    val firstLat = firstPoint.first * PI / 180
    val firstLng = firstPoint.second * PI / 180

    val secondLat = secondPoint.first * PI / 180
    val secondLng = secondPoint.second * PI / 180

    return (EARTH_RADIUS * acos(
        cos(firstLat) * cos(secondLat) *
                cos(secondLng - firstLng) + sin(firstLat) *
                sin(secondLat)
    )).round(2)
}