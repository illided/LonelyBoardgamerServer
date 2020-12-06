package com.twoilya.lonelyboardgamer.actions.commands

import com.twoilya.lonelyboardgamer.BadDataException
import kotlin.math.max
import kotlin.math.min

fun <T> List<T>.batch(offset: Int? = null, limit: Int? = null) : List<T> {
    val lowerBound = offset ?: 0
    if (lowerBound < 0)
        throw BadDataException("Offset cant be negative")

    val upperBound = lowerBound + (limit ?: this.size)
    if (upperBound < lowerBound)
        throw BadDataException("Limit can't be negative")

    return this.subList(
        min(lowerBound, max(0, this.size)),
        min(upperBound, max(0, this.size))
    )
}