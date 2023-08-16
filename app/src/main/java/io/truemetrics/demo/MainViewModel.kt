package io.truemetrics.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel : ViewModel() {

    private val mutableTime: MutableLiveData<String> = MutableLiveData()
    val time: LiveData<String> = mutableTime

    init {
        viewModelScope.launch {
            while (true) {
                mutableTime.value = Date().toString()
                delay(500)
            }
        }
    }
}