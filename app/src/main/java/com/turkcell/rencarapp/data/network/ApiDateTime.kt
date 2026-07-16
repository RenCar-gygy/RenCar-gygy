package com.turkcell.rencarapp.data.network

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val API_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC)

/** OpenAPI örneğiyle uyumlu: `2026-07-15T10:00:00.000Z` */
fun Instant.toApiDateTimeString(): String =
    API_DATE_TIME_FORMATTER.format(truncatedTo(ChronoUnit.MILLIS))
