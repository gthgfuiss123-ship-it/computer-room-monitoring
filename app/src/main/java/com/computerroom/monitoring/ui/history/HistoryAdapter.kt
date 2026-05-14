package com.computerroom.monitoring.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.computerroom.monitoring.R
import com.computerroom.monitoring.data.model.HistoryRecord
import com.computerroom.monitoring.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<HistoryRecord, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: HistoryRecord) {
            binding.tvHistoryTemp.text = String.format("%.1f°C", record.temperature)
            binding.tvHistoryHumid.text = String.format("%.0f%%", record.humidity)

            if (record.motion) {
                binding.tvHistoryMotion.text = "Chuyển động"
                binding.tvHistoryMotion.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.warning_red)
                )
            } else {
                binding.tvHistoryMotion.text = "An toàn"
                binding.tvHistoryMotion.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.safe_green)
                )
            }

            if (record.timestamp > 0) {
                val sdf = SimpleDateFormat("HH:mm:ss\ndd/MM/yyyy", Locale.getDefault())
                binding.tvHistoryTime.text = sdf.format(Date(record.timestamp * 1000L))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryRecord>() {
        override fun areItemsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: HistoryRecord, newItem: HistoryRecord): Boolean {
            return oldItem == newItem
        }
    }
}
