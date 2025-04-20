package com.bioridelabs.surfskatespot.repository

import com.bioridelabs.surfskatespot.model.Spot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class SpotRepository {
    // Instancia de Firestore
    private val firestore = FirebaseFirestore.getInstance()
    private val spotsCollection = firestore.collection("spots")

    // Obtener todos los spots
    suspend fun getAllSpots(): List<Spot> {
        return try {
            val snapshot = spotsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val spot = doc.toObject<Spot>()
                spot?.spotId = doc.id  // Asigna el ID del documento al spot
                spot
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Agregar un nuevo spot
    suspend fun addSpot(spot: Spot): Boolean {
        return try {
            val docRef = spotsCollection.add(spot).await()
            // Opcional: asignar el ID generado al objeto Spot
            spot.spotId = docRef.id
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Actualizar un spot existente
    suspend fun updateSpot(spot: Spot): Boolean {
        return try {
            val id = spot.spotId ?: return false
            spotsCollection.document(id).set(spot).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Eliminar un spot
    suspend fun deleteSpot(spotId: String): Boolean {
        return try {
            spotsCollection.document(spotId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    // Obtener un spot por su ID
    suspend fun getSpot(spotId: String): Spot? {
        return try {
            val docSnapshot = spotsCollection.document(spotId).get().await()
            if (docSnapshot.exists()) {
                val spot = docSnapshot.toObject<Spot>()
                spot?.spotId = docSnapshot.id  // Asigna el ID del documento
                spot
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

