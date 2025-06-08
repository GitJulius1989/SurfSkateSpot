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

object BitmapHelper {
    /**
     * Convierte un recurso drawable vectorial en un BitmapDescriptor.
     * Esto es útil para usar Drawables SVG como iconos de marcador en Google Maps.
     */
    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
        colorTint: Int? = null // Opcional: para tintar el icono
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, id)
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker() // Retorna un marcador por defecto si el drawable no se encuentra
        }
        // Ajustar el tamaño del drawable para que sea apropiado para un marcador si es necesario.
        // Aquí se usa el tamaño intrínseco, que para un VectorDrawable de 24dp es 24x24 pixels.
        // Si necesitas un tamaño específico para los marcadores, puedes ajustarlo aquí:
        // drawable.setBounds(0, 0, desiredWidth, desiredHeight)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        if (colorTint != null) {
            drawable.setTint(ContextCompat.getColor(context, colorTint))
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}