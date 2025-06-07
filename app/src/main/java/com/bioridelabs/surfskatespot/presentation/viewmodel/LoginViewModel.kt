package com.bioridelabs.surfskatespot.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Anota LoginViewModel con @HiltViewModel y modifica el constructor
@HiltViewModel // <--- Añade esta anotación
class LoginViewModel @Inject constructor( // <--- Añade @Inject y el parámetro
    val userRepository: UserRepository, // <--- Inyecta UserRepository
    private val firebaseAuth: FirebaseAuth // <--- También inyecta FirebaseAuth directamente aquí para Google Sign-In
) : ViewModel() {

    // LiveData para comunicar el resultado del login (email/password) a la vista
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    // LiveData para comunicar el resultado del inicio de sesión con Google a la vista
    private val _googleSignInResult = MutableLiveData<Boolean>()
    val googleSignInResult: LiveData<Boolean> get() = _googleSignInResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            _loginResult.value = result
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                // Si la autenticación con Firebase es exitosa, actualiza el LiveData
                _googleSignInResult.postValue(authResult.user != null)
            } catch (e: Exception) {
                // Manejo de errores de autenticación con Firebase
                Log.e("LoginViewModel", "Error al iniciar sesión con Google en Firebase: ${e.message}")
                _googleSignInResult.postValue(false)
            }
        }
    }
}