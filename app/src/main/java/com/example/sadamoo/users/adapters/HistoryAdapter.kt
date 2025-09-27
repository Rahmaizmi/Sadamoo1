package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemHistoryBinding
import com.example.sadamoo.users.data.Detection
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var historyList: List<Detection>,
    private val onItemClick: (Detection) -> Unit,
    private val onDeleteClick: (Detection) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Detection) {
            binding.apply {
                tvTitle.text = item.disease_name ?: "Tidak Dikenal"
                tvSubtitle.text = item.description ?: "-"
                tvDate.text = item.detectedAt
                tvStatus.text = "${"%.2f".format(item.confidence * 100)}%"

                ivIcon.setImageResource(com.example.sadamoo.R.drawable.ic_scan)
                ivIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_blue_bright))

                root.setOnClickListener {
                    onItemClick(item)
                }
                // 🔹 Klik lama untuk hapus
                root.setOnLongClickListener {
                    onDeleteClick(item)
                    true

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<Detection>) {
        historyList = newList
        notifyDataSetChanged()
    }
}
