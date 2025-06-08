// app/src/main/java/com/bioridelabs/surfskatespot/presentation/viewmodel/AddSpotViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSpotViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Estado de la UI
    private val _spotName = MutableStateFlow("")
    val spotName: StateFlow<String> = _spotName.asStateFlow()

    private val _spotDescription = MutableStateFlow("")
    val spotDescription: StateFlow<String> = _spotDescription.asStateFlow()

    private val _selectedSportTypes = MutableStateFlow(setOf<String>())
    val selectedSportTypes: StateFlow<Set<String>> = _selectedSportTypes.asStateFlow()

    private val _selectedLocation = MutableLiveData<Pair<Double, Double>?>() // Latitud, Longitud
    val selectedLocation: LiveData<Pair<Double, Double>?> = _selectedLocation

    private val _selectedPhotoUris = MutableLiveData<MutableList<Uri>>(mutableListOf()) // Uris locales de las fotos
    val selectedPhotoUris: LiveData<MutableList<Uri>> = _selectedPhotoUris

    // Eventos (SingleLiveEvent o similar para manejar eventos únicos como navegación, Toast)
    private val _addSpotResult = MutableLiveData<Boolean>()
    val addSpotResult: LiveData<Boolean> = _addSpotResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Métodos para actualizar el estado desde la UI
    fun onSpotNameChanged(name: String) {
        _spotName.value = name
    }

    fun onSpotDescriptionChanged(description: String) {
        _spotDescription.value = description
    }

    fun onSportTypeChanged(type: String, isChecked: Boolean) {
        _selectedSportTypes.value = if (isChecked) {
            _selectedSportTypes.value + type
        } else {
            _selectedSportTypes.value - type
        }
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        _selectedLocation.value = Pair(latitude, longitude)
    }

    fun addPhotoUri(uri: Uri) {
        val currentList = _selectedPhotoUris.value ?: mutableListOf()
        if (uri !in currentList) {
            currentList.add(uri)
            _selectedPhotoUris.value = currentList // Disparar la actualización del LiveData
        }
    }

    fun removePhotoUri(uri: Uri) {
        val currentList = _selectedPhotoUris.value ?: mutableListOf()
        if (currentList.remove(uri)) {
            _selectedPhotoUris.value = currentList // Disparar la actualización del LiveData
        }
    }

    // Lógica para guardar el Spot
    fun saveSpot() {
        _isLoading.value = true
        _errorMessage.value = null

        val name = _spotName.value.trim()
        val description = _spotDescription.value.trim()
        val sportTypes = _selectedSportTypes.value
        val location = _selectedLocation.value
        val photoUris = _selectedPhotoUris.value ?: emptyList()

        val userId = firebaseAuth.currentUser?.uid

        // 1. Validación
        if (userId == null) {
            _errorMessage.value = "Error: Usuario no autenticado."
            _isLoading.value = false
            _addSpotResult.value = false
            return
        }
        if (name.isBlank() || description.isBlank() || sportTypes.isEmpty() || location == null) {
            _errorMessage.value = "Por favor, completa todos los campos requeridos y selecciona una ubicación."
            _isLoading.value = false
            _addSpotResult.value = false
            return
        }

        viewModelScope.launch {
            try {
                // 2. Subir imágenes a Firebase Storage
                val uploadedPhotoUrls = mutableListOf<String>()
                if (photoUris.isNotEmpty()) {
                    val uploadTasks = photoUris.map { uri ->
                        imageStorageRepository.uploadImage(uri, userId)
                    }
                    val results = uploadTasks.mapNotNull { it } // Obtener URLs no nulas
                    if (results.size != photoUris.size) {
                        _errorMessage.value = "Error al subir una o más fotos. Inténtalo de nuevo."
                        _isLoading.value = false
                        _addSpotResult.value = false
                        return@launch
                    }
                    uploadedPhotoUrls.addAll(results)
                }

                // 3. Crear objeto Spot
                val newSpot = Spot(
                    userId = userId,
                    nombre = name,
                    descripcion = description,
                    tiposDeporte = sportTypes.toList(), // Convertir Set a List
                    latitud = location.first,
                    longitud = location.second,
                    fotosUrls = uploadedPhotoUrls
                )

                // 4. Guardar Spot en Firestore
                val success = spotRepository.addSpot(newSpot)
                if (success) {
                    _addSpotResult.value = true
                    _spotName.value = "" // Limpiar campos después de guardar
                    _spotDescription.value = ""
                    _selectedSportTypes.value = emptySet()
                    _selectedLocation.value = null
                    _selectedPhotoUris.value = mutableListOf()
                } else {
                    _errorMessage.value = "Error al guardar el spot en la base de datos."
                    _addSpotResult.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado al añadir spot: ${e.message}"
                _addSpotResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}