package com.bioridelabs.surfskatespot.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Comprime y redimensiona una imagen desde una Uri.
     * @param imageUri La Uri de la imagen a procesar.
     * @param maxWidth El ancho máximo deseado para la imagen. La altura se escala para mantener la proporción.
     * @param quality La calidad de compresión JPEG (0-100).
     * @return Un ByteArray con los datos de la imagen comprimida.
     */
    suspend fun compressImage(
        imageUri: Uri,
        maxWidth: Float = 1080.0f, // Ancho razonable para fotos de detalle
        quality: Int = 85 // Buena relación calidad/tamaño
    ): ByteArray {
        // Usamos withContext(Dispatchers.IO) porque la decodificación y compresión de bitmaps
        // son operaciones que pueden bloquear el hilo principal.
        return withContext(Dispatchers.IO) {
            // 1. Obtener el Bitmap desde la Uri
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                // Versión deprecada para APIs < 28
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // 2. Calcular nuevas dimensiones manteniendo la proporción
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

            val targetWidth = if (originalWidth > maxWidth) maxWidth.toInt() else originalWidth
            val targetHeight = (targetWidth / aspectRatio).toInt()

            // 3. Redimensionar el Bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

            // 4. Comprimir a ByteArray
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            outputStream.toByteArray()
        }
    }
}