// app/src/main/java/com/bioridelabs/surfskatespot/di/RepositoryModule.kt
package com.bioridelabs.surfskatespot.di

import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore

// Este es un módulo Hilt que le dice a Dagger cómo proporcionar ciertas dependencias.
@Module
// InstallIn(SingletonComponent::class) significa que las dependencias proporcionadas por este módulo
// estarán disponibles durante todo el ciclo de vida de la aplicación.
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // Método @Provides para la instancia de FirebaseFirestore.
    // Esto es necesario porque FirebaseFirestore.getInstance() es un método estático
    // y Hilt no puede inyectarlo directamente.
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // Método @Provides para el SpotRepository.
    // Dagger Hilt ahora sabe cómo construir SpotRepository porque puede proporcionar FirebaseFirestore.
    @Provides
    fun provideSpotRepository(firestore: FirebaseFirestore): SpotRepository {
        // Asumiendo que tu SpotRepository NO es una interfaz, sino la clase concreta.
        // Si SpotRepository fuera una interfaz con una implementación (ej. SpotRepositoryImpl),
        // tendrías que inyectar la implementación aquí:
        // return SpotRepositoryImpl(firestore)
        return SpotRepository(firestore) // Aquí se asume que tu SpotRepository tiene un constructor (firestore: FirebaseFirestore)
    }

    // Si tu SpotRepository fuera una INTERFACE, y tu implementación se llamara SpotRepositoryImpl,
    // usarías un @Binds en un módulo abstracto:
    /*
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class RepositoryBindingsModule {
        @Binds
        abstract fun bindSpotRepository(impl: SpotRepositoryImpl): SpotRepository
    }
    */
    // Pero por tu código anterior, parece que tu SpotRepository es una clase concreta.
}