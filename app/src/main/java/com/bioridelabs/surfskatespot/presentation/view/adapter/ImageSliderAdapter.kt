// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/adapter/ImageSliderAdapter.kt
package com.bioridelabs.surfskatespot.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.ItemImageSliderBinding // Crearemos este layout a continuación
import com.bumptech.glide.Glide

class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemImageSliderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.binding.imageView.context)
            .load(imageUrl)
            .centerCrop()
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size
}