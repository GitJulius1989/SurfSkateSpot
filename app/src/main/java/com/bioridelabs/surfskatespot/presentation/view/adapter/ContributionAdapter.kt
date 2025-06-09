package com.bioridelabs.surfskatespot.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.ItemContributionBinding
import com.bioridelabs.surfskatespot.domain.model.UserContribution
import java.text.SimpleDateFormat
import java.util.Locale

class ContributionAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<UserContribution, ContributionAdapter.ContributionViewHolder>(ContributionDiffCallback()) {

    class ContributionViewHolder(private val binding: ItemContributionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(contribution: UserContribution, onItemClick: (String) -> Unit) {
            when (contribution) {
                is UserContribution.CreatedSpot -> {
                    binding.tvContributionTitle.text = "Spot Creado: ${contribution.spotName}"
                    binding.tvContributionDetail.text = "Publicado el ${dateFormat.format(contribution.date)}"
                }
                is UserContribution.Valuation -> {
                    binding.tvContributionTitle.text = "Valoración: ${contribution.spotName}"
                    binding.tvContributionDetail.text = "Puntuación: ${contribution.rating} estrellas - ${dateFormat.format(contribution.date)}"
                }
                is UserContribution.Comment -> {
                    binding.tvContributionTitle.text = "Comentario en: ${contribution.spotName}"
                    binding.tvContributionDetail.text = "\"${contribution.commentText.take(40)}...\" - ${dateFormat.format(contribution.date)}"
                }
            }
            binding.root.setOnClickListener { onItemClick(contribution.spotId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributionViewHolder {
        val binding = ItemContributionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContributionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContributionViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ContributionDiffCallback : DiffUtil.ItemCallback<UserContribution>() {
        override fun areItemsTheSame(oldItem: UserContribution, newItem: UserContribution): Boolean {
            return oldItem.spotId == newItem.spotId && oldItem.javaClass == newItem.javaClass && oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: UserContribution, newItem: UserContribution): Boolean {
            return oldItem == newItem
        }
    }
}