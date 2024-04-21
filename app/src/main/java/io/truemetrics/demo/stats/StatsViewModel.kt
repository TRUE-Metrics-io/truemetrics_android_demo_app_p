package io.truemetrics.demo.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.engine.storage.db.SensorStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {
    private val mutableSensorStats: MutableLiveData<List<SensorStats>> = MutableLiveData()
    val sensorStats: LiveData<List<SensorStats>> = mutableSensorStats

    init {
        viewModelScope.launch {
            while (isActive) {
                mutableSensorStats.postValue(TruemetricsSDK.getSensorStats())
                delay(1000)
            }
        }
    }
}