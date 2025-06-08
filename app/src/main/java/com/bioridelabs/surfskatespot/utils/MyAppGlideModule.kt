// app/src/main/java/com/bioridelabs/surfskatespot/utils/MyAppGlideModule.kt
package com.bioridelabs.surfskatespot.utils

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Módulo personalizado de Glide para SurfSkateSpot.
 * Esta clase es necesaria para que Glide genere el GeneratedAppGlideModule,
 * lo que permite el uso de la API de Glide de forma fluida y optimizada.
 */
@GlideModule
class MyAppGlideModule : AppGlideModule() {
    // No necesitas implementar ningún método aquí a menos que quieras personalizar
    // el comportamiento de Glide, como el tamaño de la caché, etc.
}
