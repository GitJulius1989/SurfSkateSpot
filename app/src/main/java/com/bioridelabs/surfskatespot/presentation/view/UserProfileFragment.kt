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
import com.bioridelabs.surfskatespot.presentation.adapter.SpotAdapter
import com.bioridelabs.surfskatespot.presentation.viewmodel.UserProfileViewModel
import com.bioridelabs.surfskatespot.presentation.viewmodel.state.UserProfileState
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
    private lateinit var favoritesAdapter: SpotAdapter

    // Moderno ActivityResultLauncher para el Photo Picker.
    // No requiere permisos de almacenamiento en Android 13+ y es el enfoque recomendado.
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // El usuario seleccionó una imagen.
            viewModel.uploadProfileImage(uri)
            Snackbar.make(binding.root,
                getString(R.string.foto_actualizada_en_el_perfil_del_usuario), Snackbar.LENGTH_SHORT).show()
        } else {
            // El usuario cerró el selector sin elegir nada.
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

    // Es buena práctica llamar a la carga de datos en onResume para refrescar
    // si el usuario vuelve a este fragmento.
    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = SpotAdapter { spotId ->
            // Navegar al detalle del spot desde la lista de favoritos
            val action = UserProfileFragmentDirections.actionProfileFragmentToSpotDetailFragment(spotId)
            findNavController().navigate(action)
        }
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.editProfileImageFab.setOnClickListener {
            // Lanzar el Photo Picker
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        binding.loginRegisterButton.setOnClickListener {
            // Navegación global para ir al login desde cualquier parte de la app
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

    private fun updateUiForState(state: UserProfileState) {
        // Primero, gestionamos la visibilidad general
        binding.progressBar.visibility = if (state is UserProfileState.Loading) View.VISIBLE else View.GONE
        binding.profileContentScrollView.visibility = if (state is UserProfileState.LoggedIn) View.VISIBLE else View.GONE
        binding.loggedOutView.visibility = if (state is UserProfileState.LoggedOut) View.VISIBLE else View.GONE

        when (state) {
            is UserProfileState.LoggedIn -> {
                binding.usernameTextView.text = state.user.nombre
                binding.emailTextView.text = state.user.email

                // Formatear la fecha de registro
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val registrationDate = Date(state.user.fechaRegistro)
                binding.registrationDateTextView.text = "Miembro desde: ${sdf.format(registrationDate)}"

                // Cargar imagen de perfil con Glide o Coil (aquí un placeholder)
                // Glide.with(this).load(state.user.fotoPerfilUrl).placeholder(R.drawable.ic_account_user).into(binding.profileImageView)

                favoritesAdapter.submitList(state.favoriteSpots)
            }
            is UserProfileState.Error -> {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            // Los otros estados ya se manejan con la visibilidad
            is UserProfileState.Loading, is UserProfileState.LoggedOut -> { }
        }
    }

    private fun updateUi(isLoggedIn: Boolean) {
        binding.logoutButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.settingsButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.usernameTextView.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.emailTextView.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.loginRegisterButton.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}