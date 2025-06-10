// app/src/main/java/com/bioridelabs/surfskatespot/utils/BitmapHelper.kt
package com.bioridelabs.surfskatespot.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap

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
}