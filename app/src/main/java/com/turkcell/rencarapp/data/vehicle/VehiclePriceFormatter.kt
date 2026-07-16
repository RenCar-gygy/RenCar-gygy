package com.turkcell.rencarapp.data.vehicle

import com.turkcell.rencarapp.data.rental.RentalPlan
import java.util.Locale
import kotlin.math.roundToInt

/**
 * v2 API doğrudan [Vehicle.pricePerMinute] ve [Vehicle.pricePerHour] döner.
 * Tüm fiyat gösterimleri bu formatter üzerinden yapılmalı; günlük fiyattan türetim yalnızca
 * geriye dönük yardımcılar için korunur.
 */
object VehiclePriceFormatter {

    private const val HOURS_PER_CALENDAR_DAY = 24.0
    private const val MINUTES_PER_HOUR = 60.0

    fun hourlyPrice(pricePerDay: Double): Double = pricePerDay / HOURS_PER_CALENDAR_DAY

    fun minutelyPrice(pricePerDay: Double): Double = hourlyPrice(pricePerDay) / MINUTES_PER_HOUR

    /** Harita pini: API'deki saatlik birim fiyat (ana harita ve detay haritası aynı kaynak). */
    fun mapPinLabel(vehicle: Vehicle): String {
        val hourly = vehicle.pricePerHour.roundToInt().coerceAtLeast(1)
        return "₺$hourly"
    }

    fun hourlyLabel(vehicle: Vehicle): String =
        "₺${formatAmount(vehicle.pricePerHour)}/sa"

    fun minutelyLabel(vehicle: Vehicle): String =
        "₺${formatAmount(vehicle.pricePerMinute)}/dk"

    fun dailyLabel(vehicle: Vehicle): String =
        "₺${formatAmount(vehicle.pricePerDay)}/gün"

    fun planPriceAmount(vehicle: Vehicle, plan: RentalPlan): String = when (plan) {
        RentalPlan.PER_MINUTE -> "₺${formatAmount(vehicle.pricePerMinute)}"
        RentalPlan.HOURLY -> "₺${formatAmount(vehicle.pricePerHour)}"
        RentalPlan.DAILY -> "₺${formatAmount(vehicle.pricePerDay)}"
    }

    fun planUnitSuffix(plan: RentalPlan): String = when (plan) {
        RentalPlan.PER_MINUTE -> "/ dk"
        RentalPlan.HOURLY -> "/ sa"
        RentalPlan.DAILY -> "/ gün"
    }

    fun planPriceLabel(vehicle: Vehicle, plan: RentalPlan): String =
        "${planPriceAmount(vehicle, plan)}${planUnitSuffix(plan)}"

    fun formatMoney(amount: Double): String = "₺${formatAmount(amount)}"

    fun hourlyLabel(pricePerDay: Double): String =
        "₺${formatAmount(hourlyPrice(pricePerDay))}/sa"

    fun minutelyLabel(pricePerDay: Double): String =
        "₺${formatAmount(minutelyPrice(pricePerDay))}/dk"

    fun dailyLabel(pricePerDay: Double): String =
        "₺${formatAmount(pricePerDay)}"

    private fun formatAmount(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "%.2f", amount)
}
