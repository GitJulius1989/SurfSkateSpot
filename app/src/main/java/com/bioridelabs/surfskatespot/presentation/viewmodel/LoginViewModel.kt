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


@HiltViewModel
class LoginViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
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
                val authResult = firebaseAuth.signInWithCredential(credential).await()

                authResult.user?.let { firebaseUser ->
                    // En lugar de solo notificar el éxito, ahora gestionamos los datos del usuario.
                    userRepository.handleGoogleSignIn(firebaseUser)
                    _googleSignInResult.postValue(true)
                } ?: _googleSignInResult.postValue(false) // Si el usuario es nulo, falla.

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al iniciar sesión con Google: ${e.message}")
                _googleSignInResult.postValue(false)
            }
        }
    }
}