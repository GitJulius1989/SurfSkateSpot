// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/adapter/AddPhotoAdapter.kt
package com.bioridelabs.surfskatespot.presentation.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.ItemAddPhotoBinding
import com.bumptech.glide.Glide // Recomiendo usar Glide para cargar las miniaturas

// Adaptador para las fotos seleccionadas
class AddPhotoAdapter(private val onRemoveClick: (Uri) -> Unit) :
    ListAdapter<Uri, AddPhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

        // ViewHolder que representa cada imagen
    class PhotoViewHolder(private val binding: ItemAddPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Enlaza la URI con la vista
        fun bind(uri: Uri, onRemoveClick: (Uri) -> Unit) {
            Glide.with(binding.ivAddedPhotoThumbnail.context)
                .load(uri)
                .centerCrop()
                .into(binding.ivAddedPhotoThumbnail)

            binding.btnRemovePhoto.setOnClickListener {
                onRemoveClick(uri)
            }
        }
    }

        // Callback para detectar cambios en la lista
    class PhotoDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem // Uris son comparables directamente
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
    // Crea un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemAddPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }
    // Muestra la imagen en la posición indicada
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = getItem(position)
        holder.bind(uri, onRemoveClick)
    }
}