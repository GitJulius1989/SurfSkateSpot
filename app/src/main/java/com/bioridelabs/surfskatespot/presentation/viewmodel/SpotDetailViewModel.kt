// En: presentation/viewmodel/SpotDetailViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.Valuation
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.bioridelabs.surfskatespot.domain.repository.ValuationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val imageStorageRepository: ImageStorageRepository,
    private val valuationRepository: ValuationRepository
) : ViewModel() {

    private val _spotDetails = MutableLiveData<Spot?>()
    val spotDetails: LiveData<Spot?> = _spotDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    // Este LiveData informará a la UI si el usuario actual es el dueño del Spot.
    private val _isOwner = MutableLiveData<Boolean>()
    val isOwner: LiveData<Boolean> = _isOwner

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult

    // LiveData para el resultado de la valoración
    private val _addRatingResult = MutableLiveData<Result<Unit>>()
    val addRatingResult: LiveData<Result<Unit>> = _addRatingResult


    fun loadSpotDetails(spotId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val spot = spotRepository.getSpot(spotId)
                _spotDetails.value = spot
                spot?.let {
                    checkIfFavorite(it.spotId!!)
                    // Comprobamos si el UID del usuario logueado coincide con el del creador del spot
                    _isOwner.value = (firebaseAuth.currentUser?.uid == it.userId)
                } ?: run {
                    _isOwner.value = false
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

    // ... (El resto de métodos: checkIfFavorite, toggleFavorite, deleteSpot y clearErrorMessage se mantienen igual, ya son correctos)

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

    fun toggleFavoriteStatus() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: run {
            _errorMessage.value = "Debes iniciar sesión para marcar spots como favoritos."
            return
        }
        val spotId = _spotDetails.value?.spotId ?: return
        val currentStatus = _isFavorite.value ?: false

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // La nueva lógica: le decimos al repositorio cuál debe ser el nuevo estado.
                val success = userRepository.toggleFavoriteSpot(currentUserId, spotId, !currentStatus)
                if (success) {
                    // Actualizamos nuestro LiveData interno, que notificará a la UI.
                    _isFavorite.value = !currentStatus
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

    fun deleteCurrentSpot() {
        val spotToDelete = _spotDetails.value ?: return
        val spotId = spotToDelete.spotId ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Eliminar todas las imágenes asociadas de Firebase Storage
                spotToDelete.fotosUrls.forEach { imageUrl ->
                    imageStorageRepository.deleteImage(imageUrl)
                }

                // 2. Eliminar el documento del spot de Firestore
                val success = spotRepository.deleteSpot(spotId)

                if (success) {
                    _deleteResult.value = Result.success(Unit)
                } else {
                    throw Exception("Error al eliminar el spot de la base de datos.")
                }

            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun submitRating(spotId: String, rating: Int, comment: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Debes iniciar sesión para valorar un spot."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val valuation = Valuation(
                userId = userId,
                spotId = spotId,
                nota = rating,
                comentario = comment
            )
            val success = valuationRepository.addValuation(valuation)
            if (success) {
                _addRatingResult.value = Result.success(Unit)
                // Recargamos los detalles para mostrar la nueva media
                loadSpotDetails(spotId)
            } else {
                _addRatingResult.value = Result.failure(Exception("No se pudo enviar la valoración."))
            }
            _isLoading.value = false
        }
    }
}