// Reemplaza el contenido de tu build.gradle.kts (a nivel de PROYECTO) con esto:

plugins {
    // Aquí solo definimos los plugins usando los alias del catálogo, SIN especificar versiones.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.navigation.safeargs.kotlin) apply false // Usamos el alias
}
