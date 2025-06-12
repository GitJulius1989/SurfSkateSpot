package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.User
import com.bioridelabs.surfskatespot.domain.model.UserContribution
import com.bioridelabs.surfskatespot.domain.model.Valuation
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import android.app.Application
import com.bioridelabs.surfskatespot.utils.BitmapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val spotRepository: SpotRepository,
    private val storageRepository: ImageStorageRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    sealed class UserProfileState {
        object Loading : UserProfileState()
        data class LoggedIn(val user: User, val contributions: List<UserContribution>) : UserProfileState()
        object LoggedOut : UserProfileState()
        data class Error(val message: String) : UserProfileState()
    }

    private val _uiState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    fun loadUserContributions() {
        viewModelScope.launch {
            _uiState.value = UserProfileState.Loading

            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = UserProfileState.LoggedOut
                return@launch
            }

            try {
                val userDeferred = async { userRepository.getUser(firebaseUser.uid) }
                val createdSpotsDeferred = async {
                    firestore.collection("spots")
                        .whereEqualTo("userId", firebaseUser.uid)
                        .get().await()
                        .toObjects(Spot::class.java)
                }

                val valuationsDeferred = this.async {
                    emptyList<Valuation>()
                }

                // Espero a que todas las llamadas terminen
                val user = userDeferred.await()
                val createdSpots = createdSpotsDeferred.await()
                val valuations = valuationsDeferred.await()
                if (user == null) {
                    _uiState.value = UserProfileState.Error("No se pudieron encontrar los datos del usuario.")
                    return@launch
                }
                val contributions = mutableListOf<UserContribution>()
                createdSpots.forEach { spot ->
                    contributions.add(UserContribution.CreatedSpot(
                        spotId = spot.spotId!!,
                        spotName = spot.nombre,
                        date = spot.fechaCreacion?.time ?: System.currentTimeMillis()
                    ))
                }
                contributions.sortByDescending { it.date }

                _uiState.value = UserProfileState.LoggedIn(user, contributions)

            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error("Error al cargar el perfil: ${e.message}")
            }
        }
    }


    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                // COMPRESIÓN DE LA IMAGEN ANTES DE SUBIRLA
                val compressedBytes = withContext(Dispatchers.IO) {
                    BitmapHelper.compressImageToByteArray(
                        application, // Usar el contexto de la aplicación
                        imageUri,
                        quality = 85, // Ajustar calidad para perfil
                        maxWidth = 512, // Ajustar dimensiones para perfil
                        maxHeight = 512
                    )
                }

                if (compressedBytes == null) {
                    _uiState.value = UserProfileState.Error("Error al comprimir la imagen de perfil.")
                    return@launch
                }

                val imageUrl = storageRepository.uploadProfileImageBytes(compressedBytes, userId) // Llama al método que sube bytes
                userRepository.updateUser(userId, mapOf("fotoPerfilUrl" to imageUrl))
                loadUserContributions()
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