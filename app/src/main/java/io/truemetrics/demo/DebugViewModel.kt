package io.truemetrics.demo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.truemetrics.truemetricssdk.LogListener
import io.truemetrics.truemetricssdk.LogMessage
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.logger.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DebugViewModel : ViewModel() {

    private val mutableRecordingsCount: MutableLiveData<Long> = MutableLiveData()
    val recordingsCount: LiveData<Long> = mutableRecordingsCount

    private val mutableLogMessages: MutableLiveData<List<LogMessage>> = MutableLiveData()
    val logMessages: LiveData<List<LogMessage>> = mutableLogMessages

    private val mutableDbSize: MutableLiveData<String> = MutableLiveData()
    val dbSize: LiveData<String> = mutableDbSize

    private val mutableDbExportResult: MutableSharedFlow<FileOpStatus> = MutableSharedFlow()
    val dbExportResult = mutableDbExportResult.asSharedFlow()

    val freeStorage = TruemetricsSDK.getStorageInfo()

    val sensorInfos = TruemetricsSDK.getSensorInfos()

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

    fun saveDbToExternalStorage(context: Context, uri: Uri) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            delay(100)
            mutableDbExportResult.emit(FileOpStatus.Started)
            val result = FileLogger.exportDb(context, uri)
            if(result == null) {
                delay(1000)
                mutableDbExportResult.emit(FileOpStatus.Finished)
            } else {
                mutableDbExportResult.emit(FileOpStatus.Error(result))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        TruemetricsSDK.setLogListener(null)
    }
}

sealed class FileOpStatus {
    data object Started : FileOpStatus()
    data object Finished : FileOpStatus()
    data class Error (val message: String) : FileOpStatus()
}