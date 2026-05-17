package com.computerroom.monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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

    private val _tempMin = MutableLiveData<Float>()
    val tempMin: LiveData<Float> = _tempMin

    private val _tempMax = MutableLiveData<Float>()
    val tempMax: LiveData<Float> = _tempMax

    private val _humidMin = MutableLiveData<Float>()
    val humidMin: LiveData<Float> = _humidMin

    private val _humidMax = MutableLiveData<Float>()
    val humidMax: LiveData<Float> = _humidMax

    private var currentThresholds = ThresholdSettings()

    private val minMaxObserver = Observer<SensorData> { data ->
        trackMinMax(data)
    }

    init {
        repository.startListeningSensorData()
        repository.loadThresholds()

        sensorData.observeForever(minMaxObserver)

        _warningMessage.addSource(repository.thresholdSettings) { settings ->
            currentThresholds = settings
            sensorData.value?.let { checkWarnings(it) }
        }

        _warningMessage.addSource(sensorData) { data ->
            checkWarnings(data)
        }
    }

    private fun trackMinMax(data: SensorData) {
        val currentTempMin = _tempMin.value
        if (currentTempMin == null || data.temperature < currentTempMin) {
            _tempMin.value = data.temperature
        }
        val currentTempMax = _tempMax.value
        if (currentTempMax == null || data.temperature > currentTempMax) {
            _tempMax.value = data.temperature
        }
        val currentHumidMin = _humidMin.value
        if (currentHumidMin == null || data.humidity < currentHumidMin) {
            _humidMin.value = data.humidity
        }
        val currentHumidMax = _humidMax.value
        if (currentHumidMax == null || data.humidity > currentHumidMax) {
            _humidMax.value = data.humidity
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

    override fun onCleared() {
        super.onCleared()
        sensorData.removeObserver(minMaxObserver)
        repository.stopListeningThresholds()
    }
}
