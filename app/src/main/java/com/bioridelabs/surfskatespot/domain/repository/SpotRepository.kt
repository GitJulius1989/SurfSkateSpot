package com.bioridelabs.surfskatespot.domain.repository

import com.bioridelabs.surfskatespot.domain.model.Spot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath
import javax.inject.Inject


class SpotRepository(private val firestore: FirebaseFirestore) {

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


    suspend fun addSpot(spot: Spot): Boolean {
        return try {
            val spotData = mapOf(
                "userId" to spot.userId,
                "nombre" to spot.nombre,
                "tiposDeporte" to spot.tiposDeporte,
                "descripcion" to spot.descripcion,
                "latitud" to spot.latitud,
                "longitud" to spot.longitud,
                "fotosUrls" to spot.fotosUrls,
                "fechaCreacion" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "estado" to spot.estado,
                "averageRating" to spot.averageRating,
                "totalRatings" to spot.totalRatings
            )
            spotsCollection.add(spotData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateSpot(spot: Spot): Boolean {
        return try {
            val id = spot.spotId ?: return false
            // Reutilizo la misma lógica de mapa para asegurar consistencia
            val spotData = mapOf(
                "userId" to spot.userId,
                "nombre" to spot.nombre,
                "tiposDeporte" to spot.tiposDeporte,
                "descripcion" to spot.descripcion,
                "latitud" to spot.latitud,
                "longitud" to spot.longitud,
                "fotosUrls" to spot.fotosUrls,
                "fechaCreacion" to spot.fechaCreacion, // Al actualizar, mantenemos la fecha original
                "estado" to spot.estado,
                "averageRating" to spot.averageRating,
                "totalRatings" to spot.totalRatings
            )
            spotsCollection.document(id).set(spotData).await()
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


    /**
     * Obtiene una lista de spots por sus IDs.
     *
     * @param spotIds Lista de IDs de los spots a buscar.
     * @return Una lista de objetos Spot que coinciden con los IDs.
     */
    suspend fun getSpotsByIds(spotIds: List<String>): List<Spot> {
        if (spotIds.isEmpty()) return emptyList()

        val spotsList = mutableListOf<Spot>()
        // Firestore permite hasta 30 valores en una cláusula 'in' desde actualizaciones recientes.
        val chunks = spotIds.chunked(30)

        return try {
            for (chunk in chunks) {
                if (chunk.isEmpty()) continue
                val snapshot = spotsCollection.whereIn(FieldPath.documentId(), chunk).get().await()
                for (doc in snapshot.documents) {
                    doc.toObject<Spot>()?.let { spot ->
                        spot.spotId = doc.id
                        spotsList.add(spot)
                    }
                }
            }
            spotsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}