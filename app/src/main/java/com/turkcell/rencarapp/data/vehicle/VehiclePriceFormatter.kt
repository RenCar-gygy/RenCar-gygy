package com.turkcell.rencarapp.data.vehicle

import java.util.Locale
import kotlin.math.roundToInt

/**
 * API yalnızca [Vehicle.pricePerDay] döner ("Günlük kira ücreti").
 * Kiralama toplamı sunucuda `gün × pricePerDay` ile hesaplanır ([RentalResponseDto.totalPrice]).
 *
 * Saatlik/dakikalık etiketler API'de yok; takvim günü (24 saat) üzerinden türetilir.
 * Dakikalık/saatlik kiralama planları UI stub'tır; gerçek ücret API'da günlük bazlıdır.
 */
object VehiclePriceFormatter {

    private const val HOURS_PER_CALENDAR_DAY = 24.0
    private const val MINUTES_PER_HOUR = 60.0

    fun hourlyPrice(pricePerDay: Double): Double = pricePerDay / HOURS_PER_CALENDAR_DAY

    fun minutelyPrice(pricePerDay: Double): Double = hourlyPrice(pricePerDay) / MINUTES_PER_HOUR

    /** Harita pini için kısa saatlik fiyat etiketi (ör. ₺63). */
    fun mapPinLabel(pricePerDay: Double): String {
        val hourly = hourlyPrice(pricePerDay).roundToInt().coerceAtLeast(1)
        return "₺$hourly"
    }

    fun hourlyLabel(pricePerDay: Double): String =
        "₺${formatAmount(hourlyPrice(pricePerDay))}/sa"

    fun minutelyLabel(pricePerDay: Double): String =
        "₺${formatAmount(minutelyPrice(pricePerDay))}/dk"

    fun dailyLabel(pricePerDay: Double): String =
        "₺${formatAmount(pricePerDay)}"

    private fun formatAmount(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "%.2f", amount)
}
