package io.truemetrics.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.truemetrics.truemetricssdk.LogListener
import io.truemetrics.truemetricssdk.LogMessage
import io.truemetrics.truemetricssdk.TruemetricsSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DebugViewModel : ViewModel() {

    private val mutableRecordingsCount: MutableLiveData<Long> = MutableLiveData()
    val recordingsCount: LiveData<Long> = mutableRecordingsCount

    private val mutableLogMessages: MutableLiveData<List<LogMessage>> = MutableLiveData()
    val logMessages: LiveData<List<LogMessage>> = mutableLogMessages

    private val mutableDbSize: MutableLiveData<String> = MutableLiveData()
    val dbSize: LiveData<String> = mutableDbSize

    val freeStorage = TruemetricsSDK.getStorageInfo()

    init {
        viewModelScope.launch {
            while (isActive) {
                mutableRecordingsCount.postValue(TruemetricsSDK.getRecordingsCount())
                mutableDbSize.postValue(TruemetricsSDK.getDbSize())
                delay(1000)
            }
        }

        mutableLogMessages.postValue(TruemetricsSDK.getLogMessages())

        TruemetricsSDK.setLogListener(object : LogListener {
            override fun logMessage(logMessage: LogMessage) {

                val existing = mutableLogMessages.value?.toMutableList() ?: mutableListOf()
                existing += logMessage

                if(existing.size > 500) {
                    existing.removeAt(0)
                }

                mutableLogMessages.postValue(existing)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        TruemetricsSDK.setLogListener(null)
    }
}