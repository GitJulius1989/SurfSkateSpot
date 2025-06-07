// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/SpotDetailViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot // Importa tu data class Spot
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository // Importa tu SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository, // Inyecta UserRepository
    private val firebaseAuth: FirebaseAuth      // Inyecta FirebaseAuth
) : ViewModel() {

    private val _spotDetails = MutableLiveData<Spot?>()
    val spotDetails: LiveData<Spot?> = _spotDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    // Método para cargar los detalles del spot
    fun loadSpotDetails(spotId: String) {
        _isLoading.value = true
        _errorMessage.value = null // Limpiar errores anteriores
        viewModelScope.launch {
            try {
                val spot = spotRepository.getSpot(spotId)
                _spotDetails.value = spot
                spot?.let {
                    checkIfFavorite(it.spotId!!) // Verifica si es favorito después de cargar el spot
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los detalles del spot: ${e.message}"
                _spotDetails.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Nuevo método para verificar si el spot es favorito para el usuario actual
    private fun checkIfFavorite(spotId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _isFavorite.value = false // Si no hay usuario logueado, no puede ser favorito
            return
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUserId)
                _isFavorite.value = user?.favoritos?.contains(spotId) ?: false
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar el estado de favorito: ${e.message}"
                _isFavorite.value = false // Asumir que no es favorito en caso de error
            }
        }
    }

    // Nuevo método para alternar el estado de favorito
    fun toggleFavorite(spotId: String, currentIsFavorite: Boolean) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _errorMessage.value = "Debes iniciar sesión para marcar spots como favoritos."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val success = userRepository.toggleFavoriteSpot(currentUserId, spotId, !currentIsFavorite)
                if (success) {
                    _isFavorite.value = !currentIsFavorite // Actualiza el LiveData inmediatamente en la UI
                    _errorMessage.value = if (!currentIsFavorite) "Spot añadido a favoritos." else "Spot eliminado de favoritos."
                } else {
                    _errorMessage.value = "Error al actualizar favoritos."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar favoritos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Método para determinar si el usuario actual es el propietario del spot (si lo necesitas)
    fun isCurrentUserSpotOwner(ownerId: String): Boolean {
        return firebaseAuth.currentUser?.uid == ownerId
    }
}

