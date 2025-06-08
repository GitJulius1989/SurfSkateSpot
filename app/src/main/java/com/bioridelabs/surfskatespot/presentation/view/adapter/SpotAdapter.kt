// app/src/main/java/com/bioridelabs/surfskatespot/presentation/adapter/SpotAdapter.kt
package com.bioridelabs.surfskatespot.presentation.adapter

import android.view.LayoutInflater
import android.view.View // Importa View para View.GONE/VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.R // Importa R para acceder a recursos si fuera necesario (ej. strings o drawables)
import com.bioridelabs.surfskatespot.databinding.ItemSpotBinding // Asumimos que generarás este binding
import com.bioridelabs.surfskatespot.domain.model.Spot
// Importar Glide o Coil para cargar imágenes (lo haremos más adelante)
// import com.bumptech.glide.Glide

// Adaptador para la RecyclerView que muestra una lista de spots.
// Utiliza ListAdapter para manejar actualizaciones de la lista de manera eficiente.
// Adaptador para listar los spots
class SpotAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<Spot, SpotAdapter.SpotViewHolder>(SpotDiffCallback()) {

    // ViewHolder que mantiene las vistas de un elemento de la lista.
        // ViewHolder para cada spot
    // ViewHolder para cada spot
    class SpotViewHolder(private val binding: ItemSpotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Método para enlazar un objeto Spot con las vistas del ViewHolder.
        // Asocia los datos del spot a la vista
        fun bind(spot: Spot, onItemClick: (String) -> Unit) {
            binding.tvSpotName.text = spot.nombre
            // Si tu Spot tiene una lista de tipos (List<String>), conviértela a String
            binding.tvSpotType.text = "Tipo: ${spot.tiposDeporte.joinToString(", ")}"
            binding.tvSpotDescriptionShort.text = spot.descripcion

            // Lógica para mostrar la valoración
            if (spot.totalRatings > 0) {
                // Asegurarse de que el LinearLayout de valoración es visible
                binding.llSpotRating.visibility = View.VISIBLE
                // Formatear la valoración promedio a un decimal (ej. 4.5)
                binding.tvSpotRating.text = String.format("%.1f", spot.averageRating)
                // Mostrar el número total de valoraciones
                binding.tvTotalRatings.text = "(${spot.totalRatings} valoraciones)"
            } else {
                // Si no hay valoraciones, ocultar completamente la sección de valoración
                binding.llSpotRating.visibility = View.GONE
                // Opcional: Podrías mostrar un mensaje como "Sin valoraciones" en algún TextView
                // binding.tvSpotRating.text = binding.root.context.getString(R.string.no_ratings_available)
                // binding.tvTotalRatings.text = ""
            }

            // Cargar la imagen del spot (usar Glide/Coil cuando lo añadas)
            // if (spot.fotosUrls.isNotEmpty()) { // Usar fotosUrls si es una lista
            //     Glide.with(binding.ivSpotThumbnail.context)
            //         .load(spot.fotosUrls.first()) // Cargar la primera imagen si hay varias
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
        // Calcula diferencias entre listas
    class SpotDiffCallback : DiffUtil.ItemCallback<Spot>() {
        override fun areItemsTheSame(oldItem: Spot, newItem: Spot): Boolean {
            // Los ítems son los mismos si tienen el mismo ID de spot
            return oldItem.spotId == newItem.spotId
        }

        override fun areContentsTheSame(oldItem: Spot, newItem: Spot): Boolean {
            // Los contenidos son los mismos si todos los datos del spot son iguales.
            // Para data classes, equals() compara todas las propiedades por defecto.
            return oldItem == newItem
        }
    }

    // Se llama cuando RecyclerView necesita un nuevo ViewHolder.
    // Crea un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        // Inflar el layout del ítem usando View Binding
        val binding = ItemSpotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpotViewHolder(binding)
    }

    // Se llama para mostrar los datos en una posición específica.
    // Enlaza el ViewHolder con un spot
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        // Obtener el spot en la posición actual
        val spot = getItem(position)
        // Enlazar los datos del spot con las vistas del ViewHolder
        holder.bind(spot, onItemClick)
    }
}