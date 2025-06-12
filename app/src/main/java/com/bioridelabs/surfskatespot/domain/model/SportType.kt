package com.bioridelabs.surfskatespot.domain.model

import com.bioridelabs.surfskatespot.R

// Enum class para los tipos de deporte disponibles.
// Añado un valor de 'type' para el string que se guarda en Firestore
// y 'iconResId' para el drawable del icono correspondiente en el mapa.
enum class SportType(val type: String, val iconResId: Int) {
    SURF("Surf", R.drawable.ic_surf_place),
    SURFSKATE("Surfskate", R.drawable.ic_skateboard_place2),
    SKATEPARK("Skatepark", R.drawable.ic_skatepark_place);

    companion object {
        fun fromType(type: String): SportType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}