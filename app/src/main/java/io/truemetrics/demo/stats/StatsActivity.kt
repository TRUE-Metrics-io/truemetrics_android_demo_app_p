package io.truemetrics.demo.stats

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.truemetrics.demo.databinding.ActivitySensorStatsBinding

class StatsActivity : AppCompatActivity() {

    private val viewModel: StatsViewModel by viewModels()

    private val adapter = StatsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySensorStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.header.sensor.text = "Sensor"
        binding.header.rx.text = "RX"
        binding.header.tx.text = "TX"
        binding.header.delta.text = "Δ"

        binding.recyclerView.adapter = adapter

        viewModel.sensorStats.observe(this) {
            adapter.setItems(it)
        }
    }
}