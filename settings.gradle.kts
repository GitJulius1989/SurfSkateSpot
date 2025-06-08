pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Android & Kotlin
        id("com.android.application")      version "8.1.1" apply false
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false

        // Kotlin KAPT
        id("org.jetbrains.kotlin.kapt")    version "1.9.0" apply false

        // Hilt Gradle Plugin
        id("dagger.hilt.android.plugin")   version "2.44"  apply false

        // Google Services (Firebase)
        id("com.google.gms.google-services") version "4.3.15" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SurfSkateSpot"
include(":app")
 
