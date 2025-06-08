// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.services)
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.bioridelabs.surfskatespot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bioridelabs.surfskatespot"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    // AndroidX & Material (usando alias de libs.versions.toml)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)

    // Lifecycle & Navigation (usando alias de libs.versions.toml)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase (Auth, Firestore, Storage, Analytics) (usando alias y plataforma de libs.versions.toml)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Google Sign-In (usando alias de libs.versions.toml)
    implementation(libs.play.services.auth)

    // Coroutines + Play Services (usando alias de libs.versions.toml)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Volley (si lo necesitas y está en libs.versions.toml)
    implementation(libs.volley)

    // Hilt (usando alias de libs.versions.toml)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.annotation)
    kapt(libs.dagger.hilt.compiler)

    // Glide (usando alias de libs.versions.toml) # <-- ¡CAMBIOS AQUÍ!
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Test (usando alias de libs.versions.toml)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Splash
    implementation(libs.androidx.core.splashscreen)
}