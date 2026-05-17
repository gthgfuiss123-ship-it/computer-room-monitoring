package com.computerroom.monitoring.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class HistoryRecord(
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val timestamp: Long = 0L
) {
    constructor() : this(0f, 0f, 0L)
}
