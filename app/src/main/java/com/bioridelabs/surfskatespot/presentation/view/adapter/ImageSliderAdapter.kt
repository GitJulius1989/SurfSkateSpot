package com.bioridelabs.surfskatespot.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.ItemImageSliderBinding
import com.bumptech.glide.Glide

class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    // El ViewHolder contiene la vista de una sola imagen (usando ViewBinding).
    class ImageViewHolder(val binding: ItemImageSliderBinding) : RecyclerView.ViewHolder(binding.root)

    // Se llama para crear un nuevo ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    // Devuelve la cantidad total de imágenes.
    override fun getItemCount(): Int = imageUrls.size

    // Se llama para vincular los datos (la URL de la imagen) con la vista.
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.binding.ivSliderImage.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_layers) // Un placeholder mientras carga
            .error(R.drawable.ic_layers)       // Un icono de error si falla
            .into(holder.binding.ivSliderImage)
    }
}