package com.bioridelabs.surfskatespot.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Spot(
    @DocumentId // Indica que esta propiedad es el ID del documento en Firestore
    var spotId: String? = null,

    val userId: String = "",            // UID del usuario en Firebase Auth
    val nombre: String = "",            // nombre del spot
    val tiposDeporte: List<String> = emptyList(),
    val descripcion: String = "",       // descripción del spot
    val latitud: Double = 0.0,          // coordenada de latitud
    val longitud: Double = 0.0,         // coordenada de longitud
    val fotosUrls: List<String> = emptyList(), // lista de URLs de fotos
    @ServerTimestamp
    val fechaCreacion: Date? = null,    // Uso Date y @ServerTimestamp
    val estado: String = "activo",        // "activo", "inactivo", "eliminado"
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0
)