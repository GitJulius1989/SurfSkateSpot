// app/src/main/java/com/bioridelabs/surfskatespot/SSSApp.kt
package com.bioridelabs.surfskatespot

import android.app.Application
import dagger.hilt.android.HiltAndroidApp // ¡Importa esta anotación!

// La anotación @HiltAndroidApp genera el contenedor de componentes Hilt para toda la aplicación.
// Es el punto de entrada para la inyección de dependencias con Hilt.
@HiltAndroidApp
class SSSApp : Application() {
    // No necesitas añadir código extra aquí por ahora, solo la anotación.
    // Dagger Hilt se encargará de la inicialización.
}