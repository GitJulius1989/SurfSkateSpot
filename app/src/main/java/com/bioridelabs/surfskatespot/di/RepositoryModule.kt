package com.bioridelabs.surfskatespot.di

import com.bioridelabs.surfskatespot.domain.repository.ImageStorageRepository
import com.bioridelabs.surfskatespot.domain.repository.SpotRepository
import com.bioridelabs.surfskatespot.domain.repository.UserRepository
import com.bioridelabs.surfskatespot.domain.repository.ValuationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth // Importa FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    @Provides
    fun provideSpotRepository(firestore: FirebaseFirestore): SpotRepository {
        return SpotRepository(firestore)
    }
    @Provides
    fun provideUserRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): UserRepository {
        return UserRepository(auth, firestore)
    }
    @Provides
    fun provideImageStorageRepository(firebaseStorage: FirebaseStorage): ImageStorageRepository {
        return ImageStorageRepository(firebaseStorage)
    }
    @Provides
    fun provideValuationRepository(firestore: FirebaseFirestore): ValuationRepository {
        return ValuationRepository(firestore)
    }
}