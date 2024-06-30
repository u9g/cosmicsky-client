package dev.u9g.util

import kotlin.time.Duration

object FirmFormatters {
    fun formatCurrency(long: Long, segments: Int = 3): String {
        val α = long / 1000
        if (α != 0L) {
            return formatCurrency(α, segments) + "," + (long - α * 1000).toString().padStart(3, '0')
        }
        return long.toString()
    }

    fun formatDistance(distance: Double): String {
        if (distance < 10)
            return "%.1fm".format(distance)
        return "%dm".format(distance.toInt())
    }

    fun formatTimespan(duration: Duration): String {
        return duration.toString()
    }

}