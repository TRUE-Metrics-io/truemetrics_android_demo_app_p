package io.truemetrics.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.truemetrics.demo.FormattingUtils.formatUtc
import io.truemetrics.truemetricssdk.TruemetricsSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel : ViewModel() {

    private val mutableTime: MutableLiveData<String> = MutableLiveData()
    val time: LiveData<String> = mutableTime

    private val mutableDiffSinceStart: MutableLiveData<String> = MutableLiveData()
    val diffSinceStart: LiveData<String> = mutableDiffSinceStart

    init {
        viewModelScope.launch {
            while (isActive) {
                val date = Date()
                mutableTime.value = date.formatUtc()
                delay(30)

                if(TruemetricsSDK.isRecordingInProgress()) {
                    val startDate = TruemetricsSDK.getRecordingStartTime()
                    if(startDate != null) {
                        val diff = (date.time - startDate.time).toString()
                        if(diff.length < 4) {
                            mutableDiffSinceStart.value = "0.$diff"
                        } else {
                            val sec = diff.substring(0, diff.length - 3)
                            val millisec = diff.substring(diff.length - 3)
                            mutableDiffSinceStart.value = "$sec.$millisec"
                        }
                    }
                } else {
                    mutableDiffSinceStart.value = "--"
                }
            }
        }
    }
}