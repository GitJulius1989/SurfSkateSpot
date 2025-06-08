// En: presentation/viewmodel/UserProfileViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.bioridelabs.surfskatespot.presentation.viewmodel.state.UserProfileState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// NOTA: Necesitaremos un StorageRepository. Lo crearemos en el siguiente paso.
// Por ahora, lo comentamos para que el código compile.
// import com.bioridelabs.surfskatespot.domain.repository.StorageRepository

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val spotRepository: SpotRepository,
    private val storageRepository: ImageStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UserProfileState.Loading

            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = UserProfileState.LoggedOut
                return@launch
            }

            try {
                val user = userRepository.getUser(firebaseUser.uid)
                if (user == null) {
                    _uiState.value = UserProfileState.Error("No se pudieron encontrar los datos del usuario.")
                    return@launch
                }

                val favoriteSpots = if (user.favoritos.isNotEmpty()) {
                    // Firestore tiene una limitación de 10 elementos en la cláusula `whereIn`.
                    // Para un MVP está bien, pero para producción se necesitaría paginación o un enfoque diferente.
                    spotRepository.getSpotsByIds(user.favoritos)
                } else {
                    emptyList()
                }

                _uiState.value = UserProfileState.LoggedIn(user, favoriteSpots)
            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error("Error al cargar el perfil: ${e.message}")
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            // Mantenemos el estado actual pero mostramos que algo está pasando (ej. en el fragment)
            // o podríamos añadir un booleano `isUploading` al estado LoggedIn.

            try {
                val imageUrl = storageRepository.uploadProfileImage(userId, imageUri)
                userRepository.updateUser(userId, mapOf("fotoPerfilUrl" to imageUrl))
                loadUserProfile() // Recargar el perfil para mostrar la nueva imagen
            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error("Error al subir la imagen: ${e.message}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = UserProfileState.LoggedOut
    }
}