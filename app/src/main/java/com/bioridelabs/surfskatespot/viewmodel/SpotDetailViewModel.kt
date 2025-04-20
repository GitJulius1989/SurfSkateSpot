package com.bioridelabs.surfskatespot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.model.Spot
import com.bioridelabs.surfskatespot.repository.SpotRepository
import kotlinx.coroutines.launch

class SpotDetailViewModel : ViewModel() {

    private val spotRepository = SpotRepository()

    // LiveData que contiene la información del spot
    private val _spot = MutableLiveData<Spot>()
    val spot: LiveData<Spot> get() = _spot

    // LiveData para indicar el estado de una operación (por ejemplo, actualización)
    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    /**
     * Carga los detalles del spot dado su ID.
     */
    fun loadSpot(spotId: String) {
        viewModelScope.launch {
            val spotData = spotRepository.getSpot(spotId)
            spotData?.let {
                _spot.value = it
            }
        }
    }

    /**
     * Actualiza los datos del spot.
     */
    fun updateSpot(updatedSpot: Spot) {
        viewModelScope.launch {
            val success = spotRepository.updateSpot(updatedSpot)
            _updateResult.value = success
            if (success) {
                // Actualiza el LiveData con la versión modificada del spot
                _spot.value = updatedSpot
            }
        }
    }
}
