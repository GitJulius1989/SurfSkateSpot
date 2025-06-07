// app/src/main/java/com/bioridelabs/surfskatespot/domain/repository/ImageStorageRepository.kt
package com.bioridelabs.surfskatespot.domain.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ImageStorageRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {

    private val storageRef: StorageReference = firebaseStorage.reference

    /**
     * Sube una imagen a Firebase Storage.
     * @param imageUri La Uri local de la imagen a subir.
     * @param userId El ID del usuario que sube la imagen (para organizar en Storage).
     * @return La URL de descarga de la imagen si la subida fue exitosa, o null en caso de error.
     */
    suspend fun uploadImage(imageUri: Uri, userId: String): String? {
        return try {
            val imageFileName = "spots/${userId}/${UUID.randomUUID()}" // Ruta en Storage
            val imageRef = storageRef.child(imageFileName)

            imageRef.putFile(imageUri).await() // Sube el archivo
            val downloadUrl = imageRef.downloadUrl.await() // Obtiene la URL de descarga
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina una imagen de Firebase Storage.
     * @param imageUrl La URL de descarga de la imagen a eliminar.
     * @return true si la eliminación fue exitosa, false en caso de error.
     */
    suspend fun deleteImage(imageUrl: String): Boolean {
        return try {
            firebaseStorage.getReferenceFromUrl(imageUrl).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}