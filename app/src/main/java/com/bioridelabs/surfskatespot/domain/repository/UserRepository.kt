package com.bioridelabs.surfskatespot.domain.repository


import com.bioridelabs.surfskatespot.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor( // <--- ¡Añade @Inject aquí!
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    /**
     * Registra un nuevo usuario usando Firebase Authentication y guarda sus datos en Firestore.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @param user Objeto User con los datos adicionales del usuario.
     * @return true si el registro y guardado fueron exitosos, false en caso contrario.
     */
    suspend fun registerUser(email: String, password: String, user: User): Boolean {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return false

            // Asignas el UID directamente
            val userData = user.copy(userId = uid)

            usersCollection.document(uid).set(userData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    /**
     * Inicia sesión con email y contraseña.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @return true si el inicio de sesión fue exitoso, false en caso contrario.
     */
    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene los datos del usuario desde Firestore usando el UID.
     *
     * @param uid Identificador del usuario en Firebase Auth.
     * @return El objeto User si se encontró, o null en caso de error.
     */
    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Actualiza los datos del usuario en Firestore.
     *
     * @param uid Identificador del usuario.
     * @param updatedData Mapa con los campos a actualizar y sus nuevos valores.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    suspend fun updateUser(uid: String, updatedData: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(uid).update(updatedData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Añade o elimina un spot de la lista de favoritos del usuario.
     *
     * @param userId El UID del usuario.
     * @param spotId El ID del spot a añadir/eliminar.
     * @param isFavorite Si es true, añade; si es false, elimina.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    suspend fun toggleFavoriteSpot(userId: String, spotId: String, isFavorite: Boolean): Boolean {
        return try {
            val userRef = usersCollection.document(userId)
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val user = userSnapshot.toObject(User::class.java) ?: throw Exception("Usuario no encontrado")

                val currentFavorites = user.favoritos.toMutableList()
                if (isFavorite) {
                    if (spotId !in currentFavorites) {
                        currentFavorites.add(spotId)
                    }
                } else {
                    currentFavorites.remove(spotId)
                }
                transaction.update(userRef, "favoritos", currentFavorites)
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gestiona el inicio de sesión con Google. Comprueba si el usuario ya existe en Firestore.
     * Si no existe (es la primera vez que inicia sesión), crea su documento.
     * @param firebaseUser El objeto de usuario devuelto por Firebase Auth tras un login exitoso.
     */
    suspend fun handleGoogleSignIn(firebaseUser: FirebaseUser) {
        val userDocRef = usersCollection.document(firebaseUser.uid)

        // Usamos una transacción para asegurar que la operación de leer y escribir es atómica.
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)
            if (!snapshot.exists()) {
                // El usuario no existe en Firestore, así que lo creamos.
                val newUser = User(
                    userId = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Sin Nombre",
                    email = firebaseUser.email ?: "",
                    fotoPerfilUrl = firebaseUser.photoUrl?.toString()
                    // 'favoritos' y 'fechaRegistro' ya tienen valores por defecto en el data class.
                )
                transaction.set(userDocRef, newUser)
            }
            // Si el snapshot.exists() es true, no hacemos nada. El usuario ya estaba registrado.
        }.await()
    }
}