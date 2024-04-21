
package io.truemetrics.demo.sensors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.truemetrics.truemetricssdk.engine.sensor.model.SensorInfo

class SensorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<SensorAdapterItem<out RecyclerView.ViewHolder>> = mutableListOf()
    private val viewHolders: MutableMap<Int, SensorAdapterItem<out RecyclerView.ViewHolder>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolders.getValue(viewType).createViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindHolder(holder)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].layoutId

    fun setItems(newItems: List<SensorInfo>) {

        val mapped = newItems.map {
            SensorItem(it)
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
    private val old: List<SensorAdapterItem<out RecyclerView.ViewHolder>>,
    private val newItems: List<SensorAdapterItem<out RecyclerView.ViewHolder>>
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

abstract class SensorAdapterItem<T : RecyclerView.ViewHolder>(val id: String){

    abstract val layoutId: Int

    abstract fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): T

    abstract fun bindHolder(holder: RecyclerView.ViewHolder)
}
