// app/src/main/java/com/bioridelabs/surfskatespot/presentation/adapter/SpotAdapter.kt
package com.bioridelabs.surfskatespot.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.ItemSpotBinding // Asumimos que generarás este binding
import com.bioridelabs.surfskatespot.domain.model.Spot
// Importar Glide o Coil para cargar imágenes (lo haremos más adelante)
// import com.bumptech.glide.Glide

// Adaptador para la RecyclerView que muestra una lista de spots.
// Utiliza ListAdapter para manejar actualizaciones de la lista de manera eficiente.
class SpotAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<Spot, SpotAdapter.SpotViewHolder>(SpotDiffCallback()) {

    // ViewHolder que mantiene las vistas de un elemento de la lista.
    class SpotViewHolder(private val binding: ItemSpotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Método para enlazar un objeto Spot con las vistas del ViewHolder.
        fun bind(spot: Spot, onItemClick: (String) -> Unit) {
            binding.tvSpotName.text = spot.nombre
            // Si tu Spot tiene una lista de tipos (List<String>), conviértela a String
            binding.tvSpotType.text = "Tipo: ${spot.tiposDeporte.joinToString(", ")}" // Si tu Spot tiene un String 'tipo'
            // O si Spot tiene 'tiposDeporte: List<String>'
            // binding.tvSpotType.text = "Tipos: ${spot.tiposDeporte.joinToString(", ")}"
            binding.tvSpotDescriptionShort.text = spot.descripcion

            // Cargar la imagen del spot (usar Glide/Coil cuando lo añadas)
            // if (spot.fotoUrl != null && spot.fotoUrl.isNotEmpty()) {
            //     Glide.with(binding.ivSpotThumbnail.context)
            //         .load(spot.fotoUrl)
            //         .placeholder(R.drawable.placeholder_image) // Opcional: imagen de placeholder
            //         .error(R.drawable.error_image) // Opcional: imagen de error
            //         .into(binding.ivSpotThumbnail)
            // } else {
            //     binding.ivSpotThumbnail.setImageResource(R.drawable.default_spot_thumbnail) // Opcional: imagen por defecto
            // }

            // Configurar el click listener para el elemento completo
            binding.root.setOnClickListener {
                spot.spotId?.let { id -> onItemClick(id) }
            }
        }
    }

    // Callback para calcular las diferencias entre dos listas de spots.
    class SpotDiffCallback : DiffUtil.ItemCallback<Spot>() {
        override fun areItemsTheSame(oldItem: Spot, newItem: Spot): Boolean {
            return oldItem.spotId == newItem.spotId
        }

        override fun areContentsTheSame(oldItem: Spot, newItem: Spot): Boolean {
            return oldItem == newItem // Data class implementa equals por defecto
        }
    }

    // Se llama cuando RecyclerView necesita un nuevo ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = ItemSpotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpotViewHolder(binding)
    }

    // Se llama para mostrar los datos en una posición específica.
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = getItem(position)
        holder.bind(spot, onItemClick)
    }
}