// En una nueva carpeta "state" dentro de "presentation", o junto al ViewModel.
package com.bioridelabs.surfskatespot.presentation.viewmodel.state

import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.User

sealed class UserProfileState {
    // El estado inicial, mientras se cargan los datos.
    object Loading : UserProfileState()

    // El estado cuando el usuario ha iniciado sesión y tenemos todos sus datos.
    data class LoggedIn(val user: User, val favoriteSpots: List<Spot>) : UserProfileState()

    // El estado para cuando no hay ningún usuario con la sesión iniciada.
    object LoggedOut : UserProfileState()

    // Un estado para comunicar cualquier error que pueda ocurrir.
    data class Error(val message: String) : UserProfileState()
}