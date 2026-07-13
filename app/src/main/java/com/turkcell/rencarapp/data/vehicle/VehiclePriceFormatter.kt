package com.turkcell.rencarapp.data.vehicle

import java.util.Locale
import kotlin.math.roundToInt

/**
 * v2 API doğrudan [Vehicle.pricePerMinute] ve [Vehicle.pricePerHour] döner.
 * Geriye dönük yardımcılar günlük fiyattan türetim için korunur.
 */
object VehiclePriceFormatter {

    private const val HOURS_PER_CALENDAR_DAY = 24.0
    private const val MINUTES_PER_HOUR = 60.0

    fun hourlyPrice(pricePerDay: Double): Double = pricePerDay / HOURS_PER_CALENDAR_DAY

    fun minutelyPrice(pricePerDay: Double): Double = hourlyPrice(pricePerDay) / MINUTES_PER_HOUR

    fun mapPinLabel(vehicle: Vehicle): String {
        val hourly = vehicle.pricePerHour.roundToInt().coerceAtLeast(1)
        return "₺$hourly"
    }

    fun mapPinLabel(pricePerDay: Double): String {
        val hourly = hourlyPrice(pricePerDay).roundToInt().coerceAtLeast(1)
        return "₺$hourly"
    }

    fun hourlyLabel(vehicle: Vehicle): String =
        "₺${formatAmount(vehicle.pricePerHour)}/sa"

    fun minutelyLabel(vehicle: Vehicle): String =
        "₺${formatAmount(vehicle.pricePerMinute)}/dk"

    fun hourlyLabel(pricePerDay: Double): String =
        "₺${formatAmount(hourlyPrice(pricePerDay))}/sa"

    fun minutelyLabel(pricePerDay: Double): String =
        "₺${formatAmount(minutelyPrice(pricePerDay))}/dk"

    fun dailyLabel(pricePerDay: Double): String =
        "₺${formatAmount(pricePerDay)}"

    private fun formatAmount(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "%.2f", amount)
}
