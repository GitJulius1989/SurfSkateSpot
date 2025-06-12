package com.bioridelabs.surfskatespot.domain.model

// Clase sellada para representar contribuciones de usuario a la comunidad. Nutre el recycler de profileFragment
sealed class UserContribution {
    abstract val spotId: String
    abstract val spotName: String
    abstract val date: Long

    data class CreatedSpot(
        override val spotId: String,
        override val spotName: String,
        override val date: Long
    ) : UserContribution()

    data class Valuation(
        override val spotId: String,
        override val spotName: String,
        val rating: Int,
        override val date: Long
    ) : UserContribution()

    data class Comment(
        override val spotId: String,
        override val spotName: String,
        val commentText: String,
        override val date: Long
    ) : UserContribution()
}