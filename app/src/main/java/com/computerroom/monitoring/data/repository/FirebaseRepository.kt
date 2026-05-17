package com.computerroom.monitoring.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.computerroom.monitoring.data.model.HistoryRecord
import com.computerroom.monitoring.data.model.SensorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance()
    private val sensorRef = database.getReference("sensor")
    private val historyRef = database.getReference("history")

    private val _sensorData = MutableLiveData<SensorData>()
    val sensorData: LiveData<SensorData> = _sensorData

    private val _historyList = MutableLiveData<List<HistoryRecord>>()
    val historyList: LiveData<List<HistoryRecord>> = _historyList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var sensorListener: ValueEventListener? = null
    private var historyListener: ValueEventListener? = null
    private var historyQuery: Query? = null

    fun startListeningSensorData() {
        if (sensorListener != null) return

        sensorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                if (data != null) {
                    _sensorData.postValue(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _error.postValue("Sensor error: ${error.message}")
            }
        }
        sensorRef.addValueEventListener(sensorListener!!)
    }

    fun loadHistory() {
        historyListener?.let { listener ->
            historyQuery?.removeEventListener(listener)
        }

        val query = historyRef.orderByChild("timestamp").limitToLast(50)
        historyQuery = query

        historyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = mutableListOf<HistoryRecord>()
                for (child in snapshot.children) {
                    val record = child.getValue(HistoryRecord::class.java)
                    if (record != null) {
                        records.add(record)
                    }
                }
                records.sortByDescending { it.timestamp }
                _historyList.postValue(records)
            }

            override fun onCancelled(error: DatabaseError) {
                _error.postValue("History error: ${error.message}")
            }
        }
        query.addValueEventListener(historyListener!!)
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseRepository? = null

        fun getInstance(): FirebaseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseRepository().also { INSTANCE = it }
            }
        }
    }
}
