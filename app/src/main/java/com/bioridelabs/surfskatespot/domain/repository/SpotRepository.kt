// app/src/main/java/com/bioridelabs/surfskatespot/domain/repository/SpotRepository.kt
package com.bioridelabs.surfskatespot.domain.repository

import com.bioridelabs.surfskatespot.domain.model.Spot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class SpotRepository(private val firestore: FirebaseFirestore) { // <-- ¡CAMBIO CLAVE AQUÍ! Usa 'private val firestore: FirebaseFirestore' directamente en el constructor

    // Ya no necesitas esta línea: private val firestore = FirebaseFirestore.getInstance()
    // Porque 'firestore' ya es una propiedad del constructor y se inicializa con la que Dagger le da.

    private val spotsCollection = firestore.collection("spots") // Ahora usa la instancia inyectada

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
            // Asegúrate de que Spot tenga sus fotosUrls ya cargadas con URLs de Storage
            val docRef = spotsCollection.add(spot).await()
            // Paso 2: Actualizar el documento recién creado para añadirle su propio ID.
            val spotId = docRef.id
            spotsCollection.document(spotId).update("spotId", spotId).await()
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


    /**
     * Obtiene una lista de spots por sus IDs.
     *
     * @param spotIds Lista de IDs de los spots a buscar.
     * @return Una lista de objetos Spot que coinciden con los IDs.
     */
    suspend fun getSpotsByIds(spotIds: List<String>): List<Spot> {
        if (spotIds.isEmpty()) return emptyList()

        return try {
            val snapshot = spotsCollection.whereIn("spotId", spotIds).get().await() // Firestore solo permite hasta 10 IDs en whereIn
            // Si esperas más de 10 IDs, deberías dividir la consulta en lotes o reevaluar la estrategia.
            // Para un MVP, 10 es un buen punto de partida.
            snapshot.documents.mapNotNull { doc ->
                val spot = doc.toObject<Spot>()
                spot?.spotId = doc.id
                spot
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}