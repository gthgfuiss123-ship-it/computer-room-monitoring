package com.computerroom.monitoring.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ThresholdSettings(
    val highTemp: Float = 40f,
    val lowTemp: Float = 10f,
    val highHumid: Float = 80f,
    val lowHumid: Float = 30f
) {
    constructor() : this(40f, 10f, 80f, 30f)
}
