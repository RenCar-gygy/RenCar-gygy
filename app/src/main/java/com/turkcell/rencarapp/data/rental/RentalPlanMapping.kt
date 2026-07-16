package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.network.dto.RentalPlan as NetworkRentalPlan
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun RentalPlan.toNetwork(): NetworkRentalPlan =
    when (this) {
        RentalPlan.PER_MINUTE -> NetworkRentalPlan.PER_MINUTE
        RentalPlan.HOURLY -> NetworkRentalPlan.HOURLY
        RentalPlan.DAILY -> NetworkRentalPlan.DAILY
    }

fun RentalPlan.defaultQuoteMinutes(): Int =
    when (this) {
        RentalPlan.PER_MINUTE -> 30
        RentalPlan.HOURLY -> 60
        RentalPlan.DAILY -> 1440
    }

fun RentalPlan.durationLabel(): String =
    when (this) {
        RentalPlan.PER_MINUTE -> "30 dk"
        RentalPlan.HOURLY -> "1 sa"
        RentalPlan.DAILY -> "1 gün"
    }

fun RentalPlan.requiresScheduledEndDate(): Boolean = this == RentalPlan.DAILY

fun RentalPlan.requiresStartPhotos(): Boolean = this != RentalPlan.DAILY

private const val MINUTES_PER_CALENDAR_DAY = 1440

/**
 * DAILY planda API gün hesabı: başlanan gün tam sayılır (en az 1 gün).
 * Quote `minutes` bu takvim günü sayısına göre üretilmeli; ham dakika farkı 2 güne şişirir.
 */
fun dailyRentalDayCount(
    endDate: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Int {
    val startDate = LocalDate.now(zoneId)
    return (ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1).coerceAtLeast(1)
}

fun dailyQuoteMinutes(
    endDate: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Int = dailyRentalDayCount(endDate, zoneId) * MINUTES_PER_CALENDAR_DAY

fun dailyDurationLabel(
    endDate: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val days = dailyRentalDayCount(endDate, zoneId)
    val dayText = if (days == 1) "1 gün" else "$days gün"
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR"))
    return "$dayText · ${formatter.format(endDate)}"
}
