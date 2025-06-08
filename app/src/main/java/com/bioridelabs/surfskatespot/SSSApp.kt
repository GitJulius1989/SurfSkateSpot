// app/src/main/java/com/bioridelabs/surfskatespot/SSSApp.kt
package com.bioridelabs.surfskatespot

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SSSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa FirebaseApp antes de usar cualquier otro servicio de Firebase.
        // Esto es crucial para que App Check funcione correctamente.
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        // Usamos la constante BuildConfig.DEBUG que genera Android Studio para TU APLICACIÓN.
        // Será 'true' cuando ejecutes desde el IDE (modo depuración) y 'false' en una build firmada (release).
        if (BuildConfig.DEBUG) {
            // Entorno de depuración: Usamos el proveedor de depuración.
            // Para que este funcione, asegúrate de haber añadido el token de depuración
            // en la consola de Firebase en la sección de App Check para tu app.
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // Entorno de producción: Usamos el proveedor de Play Integrity.
            // Este proveedor verifica la integridad de la aplicación en dispositivos Android
            // que tienen los servicios de Google Play.
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}