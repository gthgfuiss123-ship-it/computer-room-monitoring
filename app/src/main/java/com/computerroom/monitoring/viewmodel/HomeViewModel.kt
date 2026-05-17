package com.computerroom.monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.computerroom.monitoring.data.model.SensorData
import com.computerroom.monitoring.data.repository.FirebaseRepository

class HomeViewModel : ViewModel() {

    private val repository = FirebaseRepository.getInstance()

    val sensorData: LiveData<SensorData> = repository.sensorData
    val error: LiveData<String> = repository.error

    private val _warningMessage = MediatorLiveData<String>()
    val warningMessage: LiveData<String> = _warningMessage

    companion object {
        private const val TEMP_THRESHOLD_HIGH = 40.0f
        private const val TEMP_THRESHOLD_LOW = 10.0f
        private const val HUMIDITY_THRESHOLD_HIGH = 80.0f
        private const val HUMIDITY_THRESHOLD_LOW = 30.0f
    }

    init {
        repository.startListeningSensorData()

        _warningMessage.addSource(sensorData) { data ->
            val warnings = mutableListOf<String>()

            if (data.temperature > TEMP_THRESHOLD_HIGH) {
                warnings.add("Nhiệt độ quá cao: ${data.temperature}°C")
            }
            if (data.temperature < TEMP_THRESHOLD_LOW) {
                warnings.add("Nhiệt độ quá thấp: ${data.temperature}°C")
            }
            if (data.humidity > HUMIDITY_THRESHOLD_HIGH) {
                warnings.add("Độ ẩm quá cao: ${data.humidity}%")
            }
            if (data.humidity < HUMIDITY_THRESHOLD_LOW) {
                warnings.add("Độ ẩm quá thấp: ${data.humidity}%")
            }

            _warningMessage.value = if (warnings.isEmpty()) "" else warnings.joinToString("\n")
        }
    }
}
