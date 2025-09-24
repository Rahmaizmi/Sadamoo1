package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemDiseaseBinding
import com.example.sadamoo.users.models.Disease

class DiseaseAdapter(
    private var diseases: List<Disease>,
    private val onItemClick: (Disease) -> Unit
) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    inner class DiseaseViewHolder(private val binding: ItemDiseaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(disease: Disease) {
            binding.apply {
                tvDiseaseName.text = disease.name
                tvDiseaseDescription.text = disease.description
                tvSeverity.text = disease.severity
                ivDiseaseImage.setImageResource(disease.imageRes)

                // Set severity color
                val severityColor = when (disease.severity) {
                    "Ringan" -> android.R.color.holo_green_light
                    "Sedang" -> android.R.color.holo_orange_light
                    "Berat" -> android.R.color.holo_red_light
                    else -> android.R.color.darker_gray
                }
                tvSeverity.setTextColor(itemView.context.getColor(severityColor))

                // Show contagious indicator
                tvContagious.text = if (disease.isContagious) "Menular" else "Tidak Menular"

                root.setOnClickListener {
                    onItemClick(disease)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val binding = ItemDiseaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiseaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount(): Int = diseases.size

    fun updateData(newDiseases: List<Disease>) {
        diseases = newDiseases
        notifyDataSetChanged()
    }
}
