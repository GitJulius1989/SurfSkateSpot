package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.SportType
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.utils.BitmapHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSpotViewModel @Inject constructor(
    private val application: Application,
    private val spotRepository: SpotRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _spotName = MutableStateFlow("")
    val spotName: StateFlow<String> = _spotName.asStateFlow()

    private val _spotDescription = MutableStateFlow("")
    val spotDescription: StateFlow<String> = _spotDescription.asStateFlow()

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

    private var spotIdForUpdate: String? = null // Para saber si estamos editando o creando

    private var spotIdToEdit: String? = null

    // El estado ya no puede ser inválido. SportType.SURF será la opción por defecto.
    private val _selectedSportType = MutableStateFlow<SportType>(SportType.SURF)
    val selectedSportType: StateFlow<SportType> = _selectedSportType.asStateFlow()

    private var existingPhotoUrls: List<String> = emptyList()



    fun onSpotNameChanged(name: String) {
        _spotName.value = name
    }

    fun onSpotDescriptionChanged(description: String) {
        _spotDescription.value = description
    }

    fun onSportTypeSelected(sportType: SportType) {
        _selectedSportType.value = sportType
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

        // --- Validaciones ---
        if (userId == null) {
            _errorMessage.value = "Error: Usuario no autenticado."
            _isLoading.value = false
            return
        }
        if (name.isBlank() || description.isBlank() || location == null) {
            _errorMessage.value = "Por favor, completa todos los campos requeridos."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                // --- Gestión de Imágenes ---
                // Separamos las nuevas fotos (tipo content://) de las que ya estaban (tipo https://)
                val newPhotoUris = photoUris.filter { it.scheme != "https" }
                val remainingOldUrls = photoUris.filter { it.scheme == "https" }.map { it.toString() }

//                // Subimos solo las imágenes nuevas
//                val newUploadedUrls = newPhotoUris.map { uri ->
//                    async { imageStorageRepository.uploadImage(uri, userId) }
//                }.awaitAll().filterNotNull()

                // Subimos solo las imágenes nuevas, COMPRIMIÉNDOLAS
                val newUploadedUrls = newPhotoUris.map { uri ->
                    async {
                        // Comprimir la imagen en un hilo de IO
                        val compressedBytes = withContext(Dispatchers.IO) {
                            BitmapHelper.compressImageToByteArray(
                                application, // Usar el contexto de la aplicación
                                uri,
                                quality = 75,
                                maxWidth = 1280,
                                maxHeight = 1280
                            )
                        }
                        if (compressedBytes != null) {
                            imageStorageRepository.uploadImageBytes(compressedBytes, userId)
                        } else {
                            // Manejar el error de compresión, posiblemente relanzando una excepción
                            throw Exception("Fallo al comprimir la imagen: $uri")
                        }
                    }
                }.awaitAll().filterNotNull()

                // Combinamos las URLs de las fotos antiguas que no se eliminaron con las nuevas
                val allPhotoUrls = remainingOldUrls + newUploadedUrls

                // --- Crear o Actualizar el Objeto Spot ---
                val spot = Spot(
                    spotId = spotIdForUpdate, // Será null si es nuevo, o tendrá un ID si se edita
                    userId = userId,
                    nombre = name,
                    descripcion = description,
                    tiposDeporte = listOf(sportType.type),
                    latitud = location.first,
                    longitud = location.second,
                    fotosUrls = allPhotoUrls
                )

                // --- Guardar en Firestore ---
                val success = if (spotIdForUpdate == null) {
                    spotRepository.addSpot(spot) // Modo CREAR
                } else {
                    spotRepository.updateSpot(spot) // Modo ACTUALIZAR
                }

                if (success) {
                    _addSpotResult.value = true
                } else {
                    _errorMessage.value = "Error al guardar el spot."
                    _addSpotResult.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _addSpotResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSpotForEditing(spotId: String) {
        // Guardamos el ID para usarlo al actualizar.
        spotIdToEdit = spotId
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val spot = spotRepository.getSpot(spotId) // Necesitas un método getSpot en tu repositorio
                if (spot != null) {
                    // Rellenamos los StateFlows y LiveData con los datos del spot.
                    _spotName.value = spot.nombre
                    _spotDescription.value = spot.descripcion
                    _selectedLocation.value = Pair(spot.latitud, spot.longitud)

                    // Para el tipo de deporte, asumimos que por ahora solo hay uno.
                    // Si tuvieras varios, aquí necesitarías una lógica más compleja.
                    val sportTypeString = spot.tiposDeporte.firstOrNull()
                    _selectedSportType.value = sportTypeString?.let { SportType.fromType(it) } ?: SportType.SURF
                    _selectedPhotoUris.value = spot.fotosUrls.map { Uri.parse(it) }.toMutableList() // Convierte URLs a Uris para mostrar en el RV
                    existingPhotoUrls = spot.fotosUrls
                } else {
                    _errorMessage.value = "No se pudo encontrar el spot para editar."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el spot: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}