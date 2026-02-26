package com.yoki.zarqaproduction.util

import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy", Locale("id", "ID"))

private val dateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm", Locale("id", "ID"))

fun Timestamp.toFormattedDate(): String =
    toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)

fun Timestamp.toFormattedDateTime(): String =
    toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .format(dateTimeFormatter)

fun Timestamp.daysSince(): Long {
    val now = System.currentTimeMillis()
    val then = toDate().time
    return (now - then) / (1000 * 60 * 60 * 24)
}
