package com.bioridelabs.surfskatespot.di

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * Comprueba si hay un usuario con sesión iniciada.
     * @return true si el usuario está autenticado, false si es un invitado.
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Devuelve el UID del usuario actual.
     * @return El String del UID si está logueado, o null si es un invitado.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}