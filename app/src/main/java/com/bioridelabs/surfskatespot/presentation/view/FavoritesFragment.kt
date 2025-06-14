package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bioridelabs.surfskatespot.databinding.FragmentSpotListBinding // Reutilizamos este binding
import com.bioridelabs.surfskatespot.presentation.adapter.SpotAdapter
import com.bioridelabs.surfskatespot.presentation.viewmodel.FavoritesViewModel // Nuevo ViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentSpotListBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by viewModels() // Usamos el nuevo ViewModel
    private lateinit var spotAdapter: SpotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpotListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        spotAdapter = SpotAdapter { spotId ->
            val action = FavoritesFragmentDirections.actionFavoritesFragmentToSpotDetailFragment(spotId)
            findNavController().navigate(action)
        }

        binding.recyclerViewSpots.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = spotAdapter
        }
    }

    private fun observeViewModel() {
        favoritesViewModel.favoriteSpots.observe(viewLifecycleOwner) { spots ->
            spotAdapter.submitList(spots)
            binding.progressBar.visibility = View.GONE
            if (spots.isEmpty()) {
                binding.tvEmptyListMessage.visibility = View.VISIBLE
            } else {
                binding.tvEmptyListMessage.visibility = View.GONE
            }
        }

        favoritesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        favoritesViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                favoritesViewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}