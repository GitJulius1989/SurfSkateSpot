package com.bioridelabs.surfskatespot.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val spotRepository = SpotRepository()

    // LiveData para exponer la lista de spots
    private val _spots = MutableLiveData<List<Spot>>()
    val spots: LiveData<List<Spot>> get() = _spots

    init {
        loadSpots()
    }

    fun loadSpots() {
        viewModelScope.launch {
            // Llamada asíncrona para obtener los spots desde el repository
            val spotList = spotRepository.getAllSpots()
            _spots.value = spotList
        }
    }
}
