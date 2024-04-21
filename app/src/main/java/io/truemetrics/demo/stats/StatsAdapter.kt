
package io.truemetrics.demo.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.truemetrics.demo.R
import io.truemetrics.demo.databinding.ItemSensorStatsBinding
import io.truemetrics.truemetricssdk.engine.storage.db.SensorStats

class StatsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<StatsAdapterItem<out RecyclerView.ViewHolder>> = mutableListOf()
    private val viewHolders: MutableMap<Int, StatsAdapterItem<out RecyclerView.ViewHolder>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolders.getValue(viewType).createViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindHolder(holder)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].layoutId

    fun setItems(newItems: List<SensorStats>) {

        val mapped = newItems.map {
            StatsItem(it)
        }

        val diffCallback = ItemsDiffCallback(items, mapped)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(mapped)

        items.forEach {
            viewHolders[it.layoutId] = it
        }

        diffResult.dispatchUpdatesTo(this)
    }
 }

class ItemsDiffCallback(
    private val old: List<StatsAdapterItem<out RecyclerView.ViewHolder>>,
    private val newItems: List<StatsAdapterItem<out RecyclerView.ViewHolder>>
    ) : DiffUtil.Callback(){

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == newItems[newItemPosition]
    }
}

abstract class StatsAdapterItem<T : RecyclerView.ViewHolder>(val id: String){

    abstract val layoutId: Int

    abstract fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): T

    abstract fun bindHolder(holder: RecyclerView.ViewHolder)
}

data class StatsItem(
    private val stats: SensorStats,
) : StatsAdapterItem<StatsItemViewHolder>(stats.hashCode().toString()) {

    override val layoutId = R.layout.item_sensor_stats

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): StatsItemViewHolder {
        return StatsItemViewHolder(ItemSensorStatsBinding.inflate(inflater, parent, false))
    }

    override fun bindHolder(holder: RecyclerView.ViewHolder) {
        (holder as StatsItemViewHolder).bind(stats)
    }
}

data class StatsItemViewHolder(
    private val binding: ItemSensorStatsBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(sensorStats: SensorStats) {
        binding.sensor.text = sensorStats.sensor
        binding.rx.text = sensorStats.rx.toString()
        binding.tx.text = sensorStats.tx.toString()
        binding.delta.text = sensorStats.delta.toString()
    }
}
