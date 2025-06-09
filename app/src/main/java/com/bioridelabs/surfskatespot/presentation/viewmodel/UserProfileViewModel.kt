// En: presentation/viewmodel/UserProfileViewModel.kt
package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.User
import com.bioridelabs.surfskatespot.domain.model.UserContribution
import com.bioridelabs.surfskatespot.domain.model.Valuation
import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.bioridelabs.surfskatespot.presentation.viewmodel.state.UserProfileState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// NOTA: Necesitaremos un StorageRepository. Lo crearemos en el siguiente paso.
// Por ahora, lo comentamos para que el código compile.
// import com.bioridelabs.surfskatespot.domain.repository.StorageRepository

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val spotRepository: SpotRepository,
    private val storageRepository: ImageStorageRepository,
    private val firestore: FirebaseFirestore // Inyectamos Firestore para consultas específicas
) : ViewModel() {

    // Cambiamos el estado para que acepte UserContribution
    sealed class UserProfileState {
        object Loading : UserProfileState()
        data class LoggedIn(val user: User, val contributions: List<UserContribution>) : UserProfileState() // <-- Cambio aquí
        object LoggedOut : UserProfileState()
        data class Error(val message: String) : UserProfileState()
    }

    private val _uiState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    // ... (El resto de tus funciones como init, uploadProfileImage, logout se quedan igual)

    // Reemplazamos loadUserProfile con esta nueva lógica
    fun loadUserContributions() {
        viewModelScope.launch {
            _uiState.value = UserProfileState.Loading

            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = UserProfileState.LoggedOut
                return@launch
            }

            try {
                // Obtenemos los datos del usuario y sus contribuciones en paralelo para más eficiencia
                val userDeferred = async { userRepository.getUser(firebaseUser.uid) }

                // 1. Obtener spots creados por el usuario
                val createdSpotsDeferred = async {
                    firestore.collection("spots")
                        .whereEqualTo("userId", firebaseUser.uid)
                        .get().await()
                        .toObjects(Spot::class.java)
                }

                // 2. Obtener valoraciones y comentarios (asumiendo que tienes estas colecciones)
                // NOTA: Para el MVP, si aún no tienes valoraciones/comentarios, puedes omitir estas llamadas.
                val valuationsDeferred = this.async {
                    // Simulación, adapta a tu modelo de Valuation
                    // firestore.collection("valuations").whereEqualTo("userId", firebaseUser.uid).get().await()
                    emptyList<Valuation>() // Placeholder
                }

                // Esperamos a que todas las llamadas terminen
                val user = userDeferred.await()
                val createdSpots = createdSpotsDeferred.await()
                val valuations = valuationsDeferred.await()

                if (user == null) {
                    _uiState.value = UserProfileState.Error("No se pudieron encontrar los datos del usuario.")
                    return@launch
                }

                // Combinamos todo en una sola lista de contribuciones
                val contributions = mutableListOf<UserContribution>()

                createdSpots.forEach { spot ->
                    contributions.add(UserContribution.CreatedSpot(
                        spotId = spot.spotId!!,
                        spotName = spot.nombre,
                        date = spot.fechaCreacion?.time ?: System.currentTimeMillis()
                    ))
                }

                // Aquí añadirías la lógica para mapear 'valuations' y 'comments' a UserContribution
                // Ejemplo:
                // valuations.forEach { valuation -> ... }

                // Ordenamos las contribuciones por fecha, de más reciente a más antigua
                contributions.sortByDescending { it.date }

                _uiState.value = UserProfileState.LoggedIn(user, contributions)

            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error("Error al cargar el perfil: ${e.message}")
            }
        }
    }


    // Los métodos que no encontraba el Fragment:
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val imageUrl = storageRepository.uploadProfileImage(userId, imageUri)
                userRepository.updateUser(userId, mapOf("fotoPerfilUrl" to imageUrl))
                loadUserContributions() // Recargar el perfil
            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error("Error al subir la imagen: ${e.message}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = UserProfileState.LoggedOut
    }
}