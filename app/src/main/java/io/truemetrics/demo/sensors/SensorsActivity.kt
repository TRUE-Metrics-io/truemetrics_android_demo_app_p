package io.truemetrics.demo.sensors

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.truemetrics.demo.databinding.ActivitySensorsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SensorsActivity : AppCompatActivity() {

    private val viewModel: SensorsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.allSensorsSwitch.isChecked = viewModel.getAllSensorsEnabled()
        binding.allSensorsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllSensorsEnabled(isChecked)
        }

        val adapter = SensorAdapter()
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.sensorInfos.collectLatest {
                    adapter.setItems(it)
                }
            }
        }
    }
}