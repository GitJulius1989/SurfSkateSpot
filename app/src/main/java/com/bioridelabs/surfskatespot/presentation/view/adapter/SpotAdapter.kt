package com.bioridelabs.surfskatespot.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.ItemSpotBinding
import com.bioridelabs.surfskatespot.domain.model.Spot

class SpotAdapter(
    private var spots: List<Spot>,
    private val onItemClick: (Spot) -> Unit
) : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    inner class SpotViewHolder(val binding: ItemSpotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(spot: Spot) {
            binding.tvSpotName.text = spot.nombre
            binding.tvSpotDescription.text = spot.descripcion
            binding.root.setOnClickListener {
                onItemClick(spot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = ItemSpotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        holder.bind(spots[position])
    }

    override fun getItemCount(): Int = spots.size

    // Método para actualizar los datos del adaptador
    fun updateData(newSpots: List<Spot>) {
        spots = newSpots
        notifyDataSetChanged()
    }
}
