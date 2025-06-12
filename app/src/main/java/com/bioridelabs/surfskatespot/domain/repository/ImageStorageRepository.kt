package com.bioridelabs.surfskatespot.domain.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

// Clase para subir y gestionar imágenes en Firebase Storage.
class ImageStorageRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    private val storageRef: StorageReference = firebaseStorage.reference

    /**
     * Sube los bytes de una imagen comprimida a Firebase Storage.
     * @param imageData Los datos de la imagen como ByteArray.
     * @param userId El ID del usuario que sube la imagen.
     * @return La URL de descarga de la imagen.
     */
    suspend fun uploadImageBytes(imageData: ByteArray, userId: String): String {
        val imageFileName = "spots/${userId}/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(imageFileName)

        // Sube el array de bytes
        imageRef.putBytes(imageData).await()
        return imageRef.downloadUrl.await().toString()
    }

    /**
     * Sube una imagen de perfil a Firebase Storage.
     * @param imageData Los datos de la imagen como ByteArray.
     * @param userId El UID del usuario.
     * @return La URL de descarga de la imagen.
     */
    suspend fun uploadProfileImageBytes(imageData: ByteArray, userId: String): String {
        val imageFileName = "profile_images/${userId}/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(imageFileName)

        imageRef.putBytes(imageData).await()
        return imageRef.downloadUrl.await().toString()
    }

    // El resto de tus métodos (uploadImage, deleteImage, etc.) pueden permanecer
    // o puedes refactorizarlos para que todos usen bytes. Por ahora, añadimos el nuevo.

    suspend fun deleteImage(imageUrl: String): Boolean {
        return try {
            firebaseStorage.getReferenceFromUrl(imageUrl).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


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

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        // Creamos una referencia única para la imagen, ej: profile_images/USER_ID/RANDOM_UUID.jpg
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = firebaseStorage.reference.child("profile_images/$userId/$fileName")

        // Subimos el archivo
        imageRef.putFile(imageUri).await()

        // Obtenemos y devolvemos la URL de descarga
        return imageRef.downloadUrl.await().toString()
    }
}