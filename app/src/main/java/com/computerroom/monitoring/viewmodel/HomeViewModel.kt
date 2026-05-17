package com.computerroom.monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.computerroom.monitoring.data.model.SensorData
import com.computerroom.monitoring.data.model.ThresholdSettings
import com.computerroom.monitoring.data.repository.FirebaseRepository

class HomeViewModel : ViewModel() {

    private val repository = FirebaseRepository.getInstance()

    val sensorData: LiveData<SensorData> = repository.sensorData
    val error: LiveData<String> = repository.error
    val thresholdSettings: LiveData<ThresholdSettings> = repository.thresholdSettings

    private val _warningMessage = MediatorLiveData<String>()
    val warningMessage: LiveData<String> = _warningMessage

    private val _criticalAlert = MutableLiveData<String?>()
    val criticalAlert: LiveData<String?> = _criticalAlert

    private var currentThresholds = ThresholdSettings()

    init {
        repository.startListeningSensorData()
        repository.loadThresholds()

        _warningMessage.addSource(repository.thresholdSettings) { settings ->
            currentThresholds = settings
            sensorData.value?.let { checkWarnings(it) }
        }

        _warningMessage.addSource(sensorData) { data ->
            checkWarnings(data)
        }
    }

    private fun checkWarnings(data: SensorData) {
        val warnings = mutableListOf<String>()
        var hasCritical = false

        if (data.temperature > currentThresholds.highTemp) {
            warnings.add("NGUY HIEM: Nhiet do qua cao: ${data.temperature}°C (nguong: ${currentThresholds.highTemp.toInt()}°C)")
            hasCritical = true
        }
        if (data.temperature < currentThresholds.lowTemp) {
            warnings.add("NGUY HIEM: Nhiet do qua thap: ${data.temperature}°C (nguong: ${currentThresholds.lowTemp.toInt()}°C)")
            hasCritical = true
        }
        if (data.humidity > currentThresholds.highHumid) {
            warnings.add("NGUY HIEM: Do am qua cao: ${data.humidity}% (nguong: ${currentThresholds.highHumid.toInt()}%)")
            hasCritical = true
        }
        if (data.humidity < currentThresholds.lowHumid) {
            warnings.add("NGUY HIEM: Do am qua thap: ${data.humidity}% (nguong: ${currentThresholds.lowHumid.toInt()}%)")
            hasCritical = true
        }

        _warningMessage.value = if (warnings.isEmpty()) "" else warnings.joinToString("\n")

        if (hasCritical) {
            _criticalAlert.value = warnings.joinToString("\n")
        }
    }

    fun clearCriticalAlert() {
        _criticalAlert.value = null
    }
}
