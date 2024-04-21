package io.truemetrics.demo.sensors

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.truemetrics.demo.R
import io.truemetrics.demo.databinding.ItemSensorBinding
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorInfo
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorStatus

data class SensorItem(
    private val info: SensorInfo,
) : SensorAdapterItem<SensorItemViewHolder>(info.sensorName.name) {

    override val layoutId = R.layout.item_sensor

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): SensorItemViewHolder {
        return SensorItemViewHolder(ItemSensorBinding.inflate(inflater, parent, false))
    }

    override fun bindHolder(holder: RecyclerView.ViewHolder) {
        (holder as SensorItemViewHolder).bind(info)
    }
}

data class SensorItemViewHolder(
    private val binding: ItemSensorBinding,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val COLOR_GREEN = Color.parseColor("#43A047")
        private val COLOR_RED = Color.parseColor("#E53935")
    }

    fun bind(info: SensorInfo) {

        binding.sensorName.text = info.sensorName.name

        when(info.sensorStatus) {

            SensorStatus.ON -> {
                binding.sensorStatus.text = "ON"
                binding.sensorFreq.text = "${info.frequency}Hz"

                binding.sensorName.setTextColor(COLOR_GREEN)
                binding.sensorStatus.setTextColor(COLOR_GREEN)
                binding.sensorFreq.setTextColor(COLOR_GREEN)
            }
            SensorStatus.OFF -> {
                binding.sensorStatus.text = "OFF"
                binding.sensorFreq.text = ""

                binding.sensorName.setTextColor(Color.WHITE)
                binding.sensorStatus.setTextColor(Color.WHITE)
                binding.sensorFreq.setTextColor(Color.WHITE)
            }
            SensorStatus.NA -> {
                binding.sensorStatus.text = "N/A"
                binding.sensorFreq.text = ""

                binding.sensorName.setTextColor(COLOR_RED)
                binding.sensorStatus.setTextColor(COLOR_RED)
                binding.sensorFreq.setTextColor(COLOR_RED)
            }
        }
    }
}
