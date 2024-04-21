package io.truemetrics.demo.sensors

import androidx.lifecycle.ViewModel
import io.truemetrics.truemetricssdk.TruemetricsSDK

class SensorsViewModel : ViewModel() {
    val sensorInfos = TruemetricsSDK.getSensorInfos()

    fun setAllSensorsEnabled(enabled: Boolean) {
        TruemetricsSDK.setAllSensorsEnabled(enabled)
    }

    fun getAllSensorsEnabled() = TruemetricsSDK.getAllSensorsEnabled()
}