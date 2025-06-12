package com.bioridelabs.surfskatespot.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import android.net.Uri


object BitmapHelper {
    /**
     * Convierte un recurso drawable vectorial en un BitmapDescriptor con un tamaño específico.
     * @param context Contexto de la aplicación.
     * @param id El ID del recurso drawable.
     * @param colorTint Color (como Resource ID) para tintar el icono (opcional).
     * @param widthDp El ancho deseado del icono en DP (opcional).
     * @param heightDp El alto deseado del icono en DP (opcional).
     */
    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
        colorTint: Int? = null,
        widthDp: Int? = null, // ¡NUEVO PARÁMETRO!
        heightDp: Int? = null  // ¡NUEVO PARÁMETRO!
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, id)
            ?: return BitmapDescriptorFactory.defaultMarker()

        // ¡LÓGICA MEJORADA PARA CALCULAR EL TAMAÑO!
        val widthInPixels: Int
        val heightInPixels: Int

        if (widthDp != null && heightDp != null) {
            // Convertimos los DP a Píxeles usando la densidad de la pantalla
            val density = context.resources.displayMetrics.density
            widthInPixels = (widthDp * density).toInt()
            heightInPixels = (heightDp * density).toInt()
        } else {
            // Si no se especifica tamaño, usamos el intrínseco (comportamiento anterior)
            widthInPixels = drawable.intrinsicWidth
            heightInPixels = drawable.intrinsicHeight
        }

        // Aplicamos el tamaño calculado
        drawable.setBounds(0, 0, widthInPixels, heightInPixels)

        if (colorTint != null) {
            drawable.setTint(ContextCompat.getColor(context, colorTint))
        }

        val bitmap = createBitmap(widthInPixels, heightInPixels)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Carga una imagen desde una Uri, la comprime y la convierte en un ByteArray.
     * @param context Contexto de la aplicación.
     * @param imageUri La Uri de la imagen a procesar.
     * @param quality La calidad de compresión (0-100).
     * @param maxWidth Ancho máximo deseado para la imagen.
     * @param maxHeight Alto máximo deseado para la imagen.
     * @return ByteArray de la imagen comprimida, o null si falla.
     */
    fun compressImageToByteArray(
        context: Context,
        imageUri: Uri,
        quality: Int = 80, // Calidad del 0 al 100
        maxWidth: Int = 1024, // Ancho máximo
        maxHeight: Int = 1024 // Alto máximo
    ): ByteArray? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true // Solo decodifica los límites (ancho/alto)
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calcula el sample size (factor de escalado)
            var actualWidth = options.outWidth
            var actualHeight = options.outHeight
            var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
            val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()

            if (actualWidth > maxWidth || actualHeight > maxHeight) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight.toFloat() / actualHeight.toFloat()
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = maxHeight
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth.toFloat() / actualWidth.toFloat()
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = maxWidth
                } else {
                    actualHeight = maxHeight
                    actualWidth = maxWidth
                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
            options.inJustDecodeBounds = false
            options.inDither = false // Deshabilita el tramado para mejor calidad
            options.inPurgeable = true // Permite que el sistema purgue el bitmap si necesita memoria
            options.inInputShareable = true // Permite compartir la referencia a los datos de entrada
            options.inTempStorage = ByteArray(16 * 1024) // Buffer temporal para la decodificación

            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            val scaledBitmap = if (bitmap != null && (bitmap.width != actualWidth || bitmap.height != actualHeight)) {
                bitmap.scale(actualWidth, actualHeight)
            } else {
                bitmap
            }

            if (scaledBitmap == null) return null

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
