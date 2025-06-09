// En: presentation/view/UserProfileFragment.kt
package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.FragmentUserProfileBinding
import com.bioridelabs.surfskatespot.domain.model.UserContribution // Asegúrate de tener esta clase
import com.bioridelabs.surfskatespot.presentation.view.adapter.ContributionAdapter // El adapter que creamos
import com.bioridelabs.surfskatespot.presentation.viewmodel.UserProfileViewModel
import com.bumptech.glide.Glide // Importa Glide para las imágenes
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var contributionAdapter: ContributionAdapter

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.uploadProfileImage(uri) // <-- Esto ahora funcionará
            Snackbar.make(binding.root, getString(R.string.foto_actualizada_en_el_perfil_del_usuario), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserContributions()
    }

    private fun setupRecyclerView() {
        contributionAdapter = ContributionAdapter { spotId ->
            val action = UserProfileFragmentDirections.actionProfileFragmentToSpotDetailFragment(spotId)
            findNavController().navigate(action)
        }
        binding.favoritesRecyclerView.apply { // Asegúrate de que el ID en el XML es correcto
            layoutManager = LinearLayoutManager(context)
            adapter = contributionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.editProfileImageFab.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout() // <-- Esto ahora funcionará
            // Opcional: navegar al login tras cerrar sesión
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        binding.loginRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUiForState(state)
                }
            }
        }
    }

    // <-- ¡CORRECCIÓN CLAVE AQUÍ! ->
    // La firma del método ahora usa el tipo anidado y el 'when' es exhaustivo.
    private fun updateUiForState(state: UserProfileViewModel.UserProfileState) {
        binding.progressBar.visibility = if (state is UserProfileViewModel.UserProfileState.Loading) View.VISIBLE else View.GONE
        binding.profileContentScrollView.visibility = if (state is UserProfileViewModel.UserProfileState.LoggedIn) View.VISIBLE else View.GONE
        binding.loggedOutView.visibility = if (state is UserProfileViewModel.UserProfileState.LoggedOut) View.VISIBLE else View.GONE

        when (state) {
            is UserProfileViewModel.UserProfileState.LoggedIn -> {
                binding.usernameTextView.text = state.user.nombre
                binding.emailTextView.text = state.user.email

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val registrationDate = Date(state.user.fechaRegistro)
                binding.registrationDateTextView.text = "Miembro desde: ${sdf.format(registrationDate)}"

                // Cargar imagen de perfil con Glide
                Glide.with(this)
                    .load(state.user.fotoPerfilUrl)
                    .placeholder(R.drawable.ic_account_user)
                    .circleCrop() // Para que la imagen sea redonda
                    .into(binding.profileImageView)

                contributionAdapter.submitList(state.contributions)
            }
            is UserProfileViewModel.UserProfileState.Error -> {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            is UserProfileViewModel.UserProfileState.Loading -> { /* Manejado por la visibilidad del progressBar */ }
            is UserProfileViewModel.UserProfileState.LoggedOut -> { /* Manejado por la visibilidad del loggedOutView */ }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}