package com.bioridelabs.surfskatespot.model

data class Spot(

    var spotId: String? = null,
    val userId: String = "",            // UID del usuario en Firebase Auth
    val nombre: String = "",            // nombre del spot
    val tipo: String = "",              // por ejemplo: "Surf", "Skate", etc.
    val descripcion: String = "",       // descripción del spot
    val latitud: Double = 0.0,          // coordenada de latitud
    val longitud: Double = 0.0,         // coordenada de longitud
    val fotoUrl: String? = null,        // URL de la foto del spot (opcional)
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: String = "activo"        // "activo", "inactivo", "eliminado"

)