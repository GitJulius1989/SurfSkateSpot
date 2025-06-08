// app/src/main/java/com/bioridelabs/surfskatespot/domain/model/SportType.kt
package com.bioridelabs.surfskatespot.domain.model

import com.bioridelabs.surfskatespot.R

// Enum class para los tipos de deporte disponibles.
// Añade un valor de 'type' para el string que se guarda en Firestore
// y 'iconResId' para el drawable del icono correspondiente en el mapa.
enum class SportType(val type: String, val iconResId: Int) { // <-- ¡ESTE ES EL CAMBIO CLAVE!
    SURF("Surf", R.drawable.ic_surf_place), // Asumiendo ic_surf_place.png
    SURFSKATE("Surfskate", R.drawable.ic_skateboard_place2), // Asumiendo ic_skateboard_place.png para surfskate
    SKATEPARK("Skatepark", R.drawable.ic_skatepark_place); // Asumiendo ic_skatepark_place.png

    companion object {
        fun fromType(type: String): SportType? {
            // 'it.type' ahora es válido porque 'type' está declarado en el enum
            return entries.firstOrNull { it.type == type }
        }
    }
}