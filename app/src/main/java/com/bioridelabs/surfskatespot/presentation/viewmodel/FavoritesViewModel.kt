// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/FavoritesViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository // Necesitamos UserRepository
import com.google.firebase.auth.FirebaseAuth // Para obtener el UID del usuario actual
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository, // Inyectamos UserRepository
    private val firebaseAuth: FirebaseAuth      // Inyectamos FirebaseAuth
) : ViewModel() {

    private val _favoriteSpots = MutableLiveData<List<Spot>>()
    val favoriteSpots: LiveData<List<Spot>> get() = _favoriteSpots

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadFavoriteSpots()
    }

    fun loadFavoriteSpots() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                _errorMessage.value = "Usuario no autenticado."
                _favoriteSpots.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val user = userRepository.getUser(currentUserId)
                if (user != null && user.favoritos.isNotEmpty()) {
                    val spots = spotRepository.getSpotsByIds(user.favoritos) // Nuevo método en SpotRepository
                    _favoriteSpots.value = spots
                } else {
                    _favoriteSpots.value = emptyList() // No hay favoritos o usuario no encontrado
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los spots favoritos: ${e.message}"
                _favoriteSpots.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}