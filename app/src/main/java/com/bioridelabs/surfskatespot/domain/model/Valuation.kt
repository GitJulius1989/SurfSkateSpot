package com.bioridelabs.surfskatespot.domain.model

data class Valuation(

    var valuationId: String? = null,
    val userId: String = "",           // UID del usuario
    val spotId: String = "",           // ID del spot
    val nota: Int = 0,                 // nota numérica (1-5)
    val comentario: String = "",       // comentario sobre el spot
    val fechaValoracion: Long = System.currentTimeMillis() // Fecha y hora de la valoración en formato timestamp(long) hace que sea mas sencilla su ordenacion


)
