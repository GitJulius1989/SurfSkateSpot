// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/SpotDetailViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository // ¡Importar esta clase!
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val imageStorageRepository: ImageStorageRepository // ¡ESTA DEPENDENCIA ES CLAVE Y DEBE ESTAR AQUÍ!
) : ViewModel() {

    private val _spotDetails = MutableLiveData<Spot?>()
    val spotDetails: LiveData<Spot?> = _spotDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    // ¡ESTE LIVE DATA ES NECESARIO PARA LA LÓGICA DE PROPIETARIO!
    private val _isOwner = MutableLiveData<Boolean>()
    val isOwner: LiveData<Boolean> = _isOwner

    // ¡ESTE LIVE DATA ES NECESARIO PARA EL RESULTADO DE LA ELIMINACIÓN!
    private val _deleteSpotResult = MutableLiveData<Boolean>()
    val deleteSpotResult: LiveData<Boolean> = _deleteSpotResult


    fun loadSpotDetails(spotId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val spot = spotRepository.getSpot(spotId)
                _spotDetails.value = spot
                spot?.let {
                    checkIfFavorite(it.spotId!!)
                    _isOwner.value = (firebaseAuth.currentUser?.uid == it.userId) // ¡ACTUALIZA EL ESTADO DE PROPIETARIO AQUÍ!
                } ?: run {
                    _isOwner.value = false // Si el spot no se encuentra, no es propietario
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los detalles del spot: ${e.message}"
                _spotDetails.value = null
                _isOwner.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkIfFavorite(spotId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _isFavorite.value = false
            return
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUserId)
                _isFavorite.value = user?.favoritos?.contains(spotId) ?: false
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar el estado de favorito: ${e.message}"
                _isFavorite.value = false
            }
        }
    }

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
                    _isFavorite.value = !currentIsFavorite
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

    // ¡ESTE MÉTODO ES NECESARIO PARA LA FUNCIÓN DE ELIMINAR SPOT!
    fun deleteSpot(spotId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val spot = spotDetails.value // Obtener el spot actual para sus fotos
                if (spot == null || spot.spotId != spotId) {
                    _errorMessage.value = "No se pudo encontrar el spot para eliminar."
                    _deleteSpotResult.value = false
                    return@launch
                }

                // 1. Eliminar fotos de Firebase Storage (si las hay)
                spot.fotosUrls.forEach { imageUrl ->
                    imageStorageRepository.deleteImage(imageUrl) // Usar la dependencia ImageStorageRepository
                }

                // 2. Eliminar spot de Firestore
                val success = spotRepository.deleteSpot(spotId)
                _deleteSpotResult.value = success
                if (!success) {
                    _errorMessage.value = "Error al eliminar el spot de la base de datos."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado al eliminar spot: ${e.message}"
                _deleteSpotResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}