package com.bioridelabs.surfskatespot.model

data class User(

    val userId: String = "",                // UID Firebase Auth
    val nombre: String = "",                // nombre del usuario
    val email: String = "",                 // correo electrónico
    val fotoPerfilUrl: String? = null,      // URL de la foto de perfil (opcional)
    val fechaRegistro: Long = System.currentTimeMillis(),
    val favoritos: List<String> = emptyList() // Lista con IDs de spots favoritos

)