// build.gradle.kts (Proyecto: SurfSkateSpot)
plugins {
    // Tus alias para los plugins de Android y Kotlin están bien si usas catálogo de versiones (libs.versions.toml)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Plugin de Hilt - Asegúrate de que la versión sea la que quieres usar (2.44 según tu error)
    id("com.google.dagger.hilt.android") version "2.44" apply false // <-- AÑADE ESTO

    // Plugin de Google Services
    id("com.google.gms.google-services") version "4.4.2" apply false // O la versión más reciente compatible
}