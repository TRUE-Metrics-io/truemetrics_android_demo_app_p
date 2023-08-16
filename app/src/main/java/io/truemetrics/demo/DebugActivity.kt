package io.truemetrics.demo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import io.truemetrics.demo.databinding.ActivityDebugBinding
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorName
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorStatus

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
            binding.recordingsInDb.text = "Recordings in DB: $it"
        }

        viewModel.logMessages.observe(this) {
            adapter.setItems(it)
        }

        val accStatus = TruemetricsSDK.getSensorStatus(SensorName.ACCELEROMETER)
        when(accStatus) {
            SensorStatus.ON -> {
                binding.sensorAccelerometer.text = "Accelerometer: ON"
                binding.sensorAccelerometer.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorAccelerometer.text = "Accelerometer: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorAccelerometer.text = "Accelerometer: N/A"
                binding.sensorAccelerometer.setTextColor(Color.parseColor("#E53935"))
            }
        }

        val magStatus = TruemetricsSDK.getSensorStatus(SensorName.MAGNETOMETER)
        when(magStatus) {
            SensorStatus.ON -> {
                binding.sensorMagnetometer.text = "Magnetometer: ON"
                binding.sensorMagnetometer.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorMagnetometer.text = "Magnetometer: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorMagnetometer.text = "Magnetometer: N/A"
                binding.sensorMagnetometer.setTextColor(Color.parseColor("#E53935"))
            }
        }

        val barStatus = TruemetricsSDK.getSensorStatus(SensorName.BAROMETER)
        when(barStatus) {
            SensorStatus.ON -> {
                binding.sensorBarometer.text = "Barometer: ON"
                binding.sensorBarometer.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorBarometer.text = "Barometer: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorBarometer.text = "Barometer: N/A"
                binding.sensorBarometer.setTextColor(Color.parseColor("#E53935"))
            }
        }

        val gyrStatus = TruemetricsSDK.getSensorStatus(SensorName.GYROSCOPE)
        when(gyrStatus) {
            SensorStatus.ON -> {
                binding.sensorGyroscope.text = "Gyroscope: ON"
                binding.sensorGyroscope.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorGyroscope.text = "Gyroscope: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorGyroscope.text = "Gyroscope: N/A"
                binding.sensorGyroscope.setTextColor(Color.parseColor("#E53935"))
            }
        }

        val locStatus = TruemetricsSDK.getSensorStatus(SensorName.LOCATION)
        when(locStatus) {
            SensorStatus.ON -> {
                binding.sensorLocation.text = "Location: ON"
                binding.sensorLocation.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorLocation.text = "Location: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorLocation.text = "Location: N/A"
                binding.sensorLocation.setTextColor(Color.parseColor("#E53935"))
            }
        }

        val gnssStatus = TruemetricsSDK.getSensorStatus(SensorName.GNSS)
        when(gnssStatus) {
            SensorStatus.ON -> {
                binding.sensorGnss.text = "GNSS: ON"
                binding.sensorGnss.setTextColor(Color.parseColor("#43A047"))
            }
            SensorStatus.OFF -> {
                binding.sensorGnss.text = "GNSS: OFF"
            }
            SensorStatus.NA -> {
                binding.sensorGnss.text = "GNSS: N/A"
                binding.sensorGnss.setTextColor(Color.parseColor("#E53935"))
            }
        }
    }
}