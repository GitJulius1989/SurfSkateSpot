package com.bioridelabs.surfskatespot.repository


import com.bioridelabs.surfskatespot.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    // Instancias de Firebase Auth y Firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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
}