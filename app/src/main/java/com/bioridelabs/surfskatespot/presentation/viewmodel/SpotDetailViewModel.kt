// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/SpotDetailViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot // Importa tu data class Spot
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository // Importa tu SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository // Inyecta tu SpotRepository
) : ViewModel() {

    private val _spotDetails = MutableLiveData<Spot?>()
    val spotDetails: LiveData<Spot?> = _spotDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Método para cargar los detalles del spot
    fun loadSpotDetails(spotId: String) {
        _isLoading.value = true
        _errorMessage.value = null // Limpiar errores anteriores
        viewModelScope.launch {
            try {
                val spot = spotRepository.getSpot(spotId) // <-- ¡CAMBIO AQUÍ! Usa tu método 'getSpot'
                _spotDetails.value = spot
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los detalles del spot: ${e.message}"
                _spotDetails.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Método para determinar si el usuario actual es el propietario del spot (requiere FirebaseAuth o AuthRepository)
    // val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance() // Si no lo tienes inyectado con Hilt
    // fun isCurrentUserSpotOwner(ownerId: String): Boolean {
    //     return firebaseAuth.currentUser?.uid == ownerId
    // }
}