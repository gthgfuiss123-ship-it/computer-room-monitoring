package com.computerroom.monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.computerroom.monitoring.data.model.HistoryRecord
import com.computerroom.monitoring.data.repository.FirebaseRepository

class HistoryViewModel : ViewModel() {

    private val repository = FirebaseRepository.getInstance()

    val historyList: LiveData<List<HistoryRecord>> = repository.historyList
    val error: LiveData<String> = repository.error

    init {
        repository.loadHistory()
    }

    fun refresh() {
        repository.loadHistory()
    }

    fun loadHistoryByDate(dateMillis: Long) {
        repository.loadHistory(dateMillis)
    }
}
