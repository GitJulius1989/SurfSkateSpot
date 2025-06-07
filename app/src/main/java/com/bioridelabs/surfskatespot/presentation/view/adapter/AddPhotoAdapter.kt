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

class AddPhotoAdapter(private val onRemoveClick: (Uri) -> Unit) :
    ListAdapter<Uri, AddPhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    class PhotoViewHolder(private val binding: ItemAddPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

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

    class PhotoDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem // Uris son comparables directamente
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemAddPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = getItem(position)
        holder.bind(uri, onRemoveClick)
    }
}