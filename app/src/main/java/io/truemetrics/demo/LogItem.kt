package io.truemetrics.demo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.truemetrics.demo.databinding.ItemLogBinding
import io.truemetrics.truemetricssdk.LogMessage

data class LogItem(
    private val message: LogMessage,
) : LogAdapterItem<LogItemViewHolder>(message.hashCode().toString()) {

    override val layoutId = R.layout.item_log

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): LogItemViewHolder {
        return LogItemViewHolder(ItemLogBinding.inflate(inflater, parent, false))
    }

    override fun bindHolder(holder: RecyclerView.ViewHolder) {
        (holder as LogItemViewHolder).bind(message)
    }
}

data class LogItemViewHolder(
    private val binding: ItemLogBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: LogMessage) {
        binding.time.text = message.time
        binding.tag.text = message.tag
        binding.level.text = message.level
        binding.message.text = message.message

        if(message.tag == "UploadExecutor" || message.tag == "DefaultRemoteDataStore") {
            binding.root.setBackgroundColor(Color.parseColor("#22C5E1A5"))
        } else {
            binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
