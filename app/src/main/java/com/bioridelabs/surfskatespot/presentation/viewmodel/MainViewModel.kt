// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/MainViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel // ¡Importa HiltViewModel!
import kotlinx.coroutines.launch
import javax.inject.Inject // ¡Importa Inject!

// Anota MainViewModel con @HiltViewModel para que Hilt pueda inyectarlo
@HiltViewModel
class MainViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    private val _spots = MutableLiveData<List<Spot>>()
    val spots: LiveData<List<Spot>> get() = _spots

    // ¡AÑADE ESTAS PROPIEDADES PARA EL ESTADO DE CARGA!
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ¡AÑADE ESTAS PROPIEDADES PARA LOS MENSAJES DE ERROR!
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadSpots()
    }

    fun loadSpots() {
        _isLoading.value = true // Establecer a true al inicio de la carga
        _errorMessage.value = null // Limpiar cualquier error anterior
        viewModelScope.launch {
            try {
                val spotList = spotRepository.getAllSpots()
                _spots.value = spotList
            } catch (e: Exception) {
                // Capturar y manejar cualquier excepción durante la carga de spots
                _errorMessage.value = "Error al cargar los spots: ${e.message}"
                _spots.value = emptyList() // O dejarlo como estaba si quieres mostrar un estado vacío diferente
            } finally {
                _isLoading.value = false // Establecer a false cuando la carga finaliza
            }
        }
    }
    // ¡AÑADE ESTE MÉTODO PARA LIMPIAR EL MENSAJE DE ERROR!
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}