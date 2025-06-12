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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    private val _spots = MutableLiveData<List<Spot>>()
    val spots: LiveData<List<Spot>> get() = _spots

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _areSpotsVisible = MutableLiveData<Boolean>(true)

    val areSpotsVisible: LiveData<Boolean> get() = _areSpotsVisible

    init {
        loadSpots()
    }

    fun loadSpots() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val spotList = spotRepository.getAllSpots()
                _spots.value = spotList
            } catch (e: Exception) {

                _errorMessage.value = "Error al cargar los spots: ${e.message}"
                _spots.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Alterna el estado de visibilidad de los spots.
     * Si el valor actual es true, lo cambia a false, y viceversa.
     */
    fun toggleSpotsVisibility() {
        _areSpotsVisible.value = !(_areSpotsVisible.value ?: true)
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}