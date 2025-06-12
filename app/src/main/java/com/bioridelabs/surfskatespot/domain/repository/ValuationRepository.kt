package com.bioridelabs.surfskatespot.domain.repository

import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.Valuation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ValuationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val spotsCollection = firestore.collection("spots")
    private val valuationsCollection = firestore.collection("valuations")

    /**
     * Añade una nueva valoración y actualiza la media del spot de forma atómica.
     * @param valuation El objeto Valuation con los datos de la nueva valoración.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    suspend fun addValuation(valuation: Valuation): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val spotRef = spotsCollection.document(valuation.spotId)
                val spotSnapshot = transaction.get(spotRef)
                val spot = spotSnapshot.toObject(Spot::class.java)
                    ?: throw Exception("El Spot con ID ${valuation.spotId} no existe.")

                // Lógica para calcular la nueva valoración media
                val oldTotalRatings = spot.totalRatings
                val oldAverageRating = spot.averageRating

                val newTotalRatings = oldTotalRatings + 1
                // Fórmula para calcular la nueva media sin tener que sumar todas las notas anteriores
                val newAverageRating = (oldAverageRating * oldTotalRatings + valuation.nota) / newTotalRatings

                // Añado la nueva valoración a la colección 'valuations'
                val newValuationRef = valuationsCollection.document()
                transaction.set(newValuationRef, valuation)

                // Actualizo el spot con la nueva media y el nuevo total de valoraciones
                transaction.update(
                    spotRef,
                    "averageRating", newAverageRating,
                    "totalRatings", newTotalRatings
                )
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}