package io.truemetrics.demo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import io.truemetrics.demo.databinding.ActivityDebugBinding
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorName
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    private val viewModel: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LogAdapter()

        var canScrollVertically = false

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.scrollToBottom.isVisible = false
        binding.scrollToBottom.setOnClickListener {
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
            canScrollVertically = false
            binding.scrollToBottom.isVisible = false
        }

        binding.recyclerView.adapter = adapter
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.d("DebugActivity", "onItemRangeInserted = $canScrollVertically")

                if(!canScrollVertically) {
                    binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })

        binding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == SCROLL_STATE_IDLE) {
                    canScrollVertically = recyclerView.canScrollVertically(1)
                    Log.d("DebugActivity", "canScrollVertically = $canScrollVertically")
                    binding.scrollToBottom.isVisible = canScrollVertically
                }
            }
        })

        viewModel.recordingsCount.observe(this) {
            binding.recordingsInDb.text = "DB rows: $it"
        }

        viewModel.logMessages.observe(this) {
            adapter.setItems(it)
        }

        viewModel.dbSize.observe(this) {
            binding.dbSize.text = "DB size: $it"
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.freeStorage.collectLatest {
                    binding.freeStorage.text = "Free storage: $it"
                }
            }
        }

        updateSensorStatus(SensorName.ACCELEROMETER, binding.sensorAccelerometer)
        updateSensorStatus(SensorName.MAGNETOMETER, binding.sensorMagnetometer)
        updateSensorStatus(SensorName.BAROMETER, binding.sensorBarometer)
        updateSensorStatus(SensorName.GYROSCOPE, binding.sensorGyroscope)
        updateSensorStatus(SensorName.LOCATION, binding.sensorLocation)
        updateSensorStatus(SensorName.GNSS, binding.sensorGnss)
        updateSensorStatus(SensorName.STEP_COUNTER, binding.sensorStepCounter)
        updateSensorStatus(SensorName.MOTION_MODE, binding.sensorMotion)
        updateSensorStatus(SensorName.WIFI_SIGNAL, binding.sensorWifi)
        updateSensorStatus(SensorName.MOBILE_DATA_SIGNAL, binding.sensorMobileData)
        updateSensorStatus(SensorName.RAW_LOCATION, binding.sensorRawLocation)
    }

    private fun updateSensorStatus(sensorName: SensorName, sensorLabel: TextView) {
        val status = TruemetricsSDK.getSensorStatus(sensorName)
        when(status) {
            SensorStatus.ON -> {
                sensorLabel.text = "$sensorName: ON"
                sensorLabel.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                sensorLabel.text = "$sensorName: OFF"
            }
            SensorStatus.NA -> {
                sensorLabel.text = "$sensorName: N/A"
                sensorLabel.setTextColor(Color.parseColor("#E53935"))
            }
        }
    }
}