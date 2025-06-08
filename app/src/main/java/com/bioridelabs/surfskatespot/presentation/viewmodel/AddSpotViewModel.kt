// En: presentation/viewmodel/AddSpotViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.SportType
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.utils.ImageOptimizer
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSpotViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val imageOptimizer: ImageOptimizer, // ¡Inyectamos el optimizador!
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // ... (El resto de tus LiveData y StateFlows se mantienen igual)
    private val _spotName = MutableStateFlow("")
    val spotName: StateFlow<String> = _spotName.asStateFlow()

    private val _spotDescription = MutableStateFlow("")
    val spotDescription: StateFlow<String> = _spotDescription.asStateFlow()

    private val _selectedSportType = MutableLiveData<SportType?>(null)
    val selectedSportType: MutableLiveData<SportType?> = _selectedSportType

    private val _selectedLocation = MutableLiveData<Pair<Double, Double>?>() // Latitud, Longitud
    val selectedLocation: LiveData<Pair<Double, Double>?> = _selectedLocation

    private val _selectedPhotoUris = MutableLiveData<MutableList<Uri>>(mutableListOf()) // Uris locales de las fotos
    val selectedPhotoUris: LiveData<MutableList<Uri>> = _selectedPhotoUris

    private val _addSpotResult = MutableLiveData<Boolean>()
    val addSpotResult: LiveData<Boolean> = _addSpotResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    // ... (El resto de tus métodos 'on...Changed' y 'add/remove PhotoUri' se mantienen igual)
    fun onSpotNameChanged(name: String) {
        _spotName.value = name
    }

    fun onSpotDescriptionChanged(description: String) {
        _spotDescription.value = description
    }

    fun onSportTypeSelected(sportType: SportType) {
        if (_selectedSportType.value == sportType) {
            _selectedSportType.value = null
        } else {
            _selectedSportType.value = sportType
        }
    }
    fun onLocationSelected(latitude: Double, longitude: Double) {
        _selectedLocation.value = Pair(latitude, longitude)
    }

    fun addPhotoUri(uri: Uri) {
        val currentList = _selectedPhotoUris.value ?: mutableListOf()
        if (uri !in currentList) {
            currentList.add(uri)
            _selectedPhotoUris.value = currentList
        }
    }

    fun removePhotoUri(uri: Uri) {
        val currentList = _selectedPhotoUris.value ?: mutableListOf()
        if (currentList.remove(uri)) {
            _selectedPhotoUris.value = currentList
        }
    }

    fun saveSpot() {
        _isLoading.value = true
        _errorMessage.value = null

        val name = _spotName.value.trim()
        val description = _spotDescription.value.trim()
        val sportType = _selectedSportType.value
        val location = _selectedLocation.value
        val photoUris = _selectedPhotoUris.value ?: emptyList()
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            _errorMessage.value = "Error: Usuario no autenticado."
            _isLoading.value = false
            return
        }
        if (name.isBlank() || description.isBlank() || sportType == null || location == null) {
            _errorMessage.value = "Por favor, completa todos los campos requeridos."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                // 2. Optimizar y subir imágenes en paralelo para máxima eficiencia
                val uploadedPhotoUrls = if (photoUris.isNotEmpty()) {
                    val uploadTasks = photoUris.map { uri ->
                        // Creamos una tarea asíncrona para cada imagen
                        async {
                            val compressedData = imageOptimizer.compressImage(uri)
                            imageStorageRepository.uploadImageBytes(compressedData, userId)
                        }
                    }
                    // Esperamos a que todas las tareas de subida finalicen
                    uploadTasks.awaitAll()
                } else {
                    emptyList()
                }

                // 3. Crear objeto Spot
                val newSpot = Spot(
                    userId = userId,
                    nombre = name,
                    descripcion = description,
                    tiposDeporte = listOf(sportType.type),
                    latitud = location.first,
                    longitud = location.second,
                    fotosUrls = uploadedPhotoUrls
                )

                // 4. Guardar Spot en Firestore
                val success = spotRepository.addSpot(newSpot)
                if (success) {
                    _addSpotResult.value = true
                    // Limpiar el estado para el siguiente spot
                } else {
                    _errorMessage.value = "Error al guardar el spot en la base de datos."
                    _addSpotResult.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado al añadir spot: ${e.message}"
                _addSpotResult.value = false
                // Aquí podrías borrar las imágenes ya subidas si la operación falla a mitad
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}