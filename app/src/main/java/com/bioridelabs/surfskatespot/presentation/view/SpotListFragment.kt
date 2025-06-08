// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/SpotListFragment.kt
package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bioridelabs.surfskatespot.databinding.FragmentSpotListBinding
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.presentation.adapter.SpotAdapter // Asumimos que crearemos este adaptador
import com.bioridelabs.surfskatespot.presentation.viewmodel.MainViewModel // Importa tu MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpotListFragment : Fragment() {

    private var _binding: FragmentSpotListBinding? = null
    private val binding get() = _binding!!

    // Inyecta el MainViewModel que ya creamos y que carga los spots
    private val mainViewModel: MainViewModel by viewModels()

    // Declaramos el adaptador de la RecyclerView
    private lateinit var spotAdapter: SpotAdapter
    // Infla la lista de spots
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpotListBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Configura la RecyclerView y observa datos
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar la RecyclerView
        setupRecyclerView()

        // 2. Observar los LiveData del ViewModel
        observeViewModel()

        // Si es la primera vez que se carga el fragmento, pedir los spots.
        // El init block de MainViewModel ya llama a loadSpots(), así que esto es redundante,
        // pero podrías tener un refresh button aquí.
        // mainViewModel.loadSpots()
    }
    // Prepara la lista de elementos
    private fun setupRecyclerView() {
        spotAdapter = SpotAdapter { spotId ->
            // Implementar la navegación a SpotDetailFragment al hacer clic en un spot
            val action = SpotListFragmentDirections.actionSpotListFragmentToSpotDetailFragment(spotId) // <-- ¡CORRECCIÓN AQUÍ!
            // Si la lista es el destino de inicio o se puede acceder directamente,
            // la acción debería ser algo como SpotListFragmentDirections.actionSpotListFragmentToSpotDetailFragment(spotId)
            findNavController().navigate(action)
        }

        binding.recyclerViewSpots.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = spotAdapter
        }
    }
    // Observa los datos del ViewModel
    private fun observeViewModel() {
        mainViewModel.spots.observe(viewLifecycleOwner) { spots ->
            // Cuando la lista de spots cambia en el ViewModel, actualiza el adaptador
            spotAdapter.submitList(spots) // Usa submitList si tu adaptador es ListAdapter
            binding.progressBar.visibility = View.GONE // Oculta el ProgressBar
            if (spots.isEmpty()) {
                binding.tvEmptyListMessage.visibility = View.VISIBLE
            } else {
                binding.tvEmptyListMessage.visibility = View.GONE
            }
        }

        // Observar el estado de carga (opcional, pero buena práctica)
        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar mensajes de error
        mainViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                mainViewModel.clearErrorMessage() //
            }
        }
    }
    // Libera el binding del fragmento
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}