package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.network.dto.RentalPlan as NetworkRentalPlan

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
