package com.bioridelabs.surfskatespot.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.FragmentLoginBinding
import com.bioridelabs.surfskatespot.domain.model.User
import com.bioridelabs.surfskatespot.presentation.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

// Fragmento para la pantalla de inicio de sesión y registro de usuarios.
class LoginFragment : Fragment() {

    // _binding es una propiedad nullable que contiene la instancia de FragmentLoginBinding.
    // Se inicializa en onCreateView y se limpia en onDestroyView para evitar fugas de memoria.
    private var _binding: FragmentLoginBinding? = null
    // binding es una propiedad de solo lectura que proporciona acceso seguro a la vista.
    // Solo se debe acceder a ella después de que _binding haya sido inicializado.
    private val binding get() = _binding!!

    // Inyección del ViewModel usando viewModels() para que el ciclo de vida del ViewModel
    // esté vinculado al Fragment.
    private val loginViewModel: LoginViewModel by viewModels()

    // Cliente de Google Sign-In para manejar el flujo de autenticación de Google.
    private lateinit var googleSignInClient: GoogleSignInClient

    // Launcher para iniciar la actividad de inicio de sesión de Google y obtener su resultado.
    // Este es el enfoque moderno para manejar resultados de actividades.
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    /**
     * Se llama para que el fragmento instancie su diseño de interfaz de usuario.
     * @param inflater El objeto LayoutInflater que se puede usar para inflar cualquier vista en el fragmento.
     * @param container Si no es nulo, este es el padre al que se adjuntará la jerarquía de vistas inflada.
     * @param savedInstanceState Si no es nulo, este fragmento está siendo recreado a partir de un estado guardado previamente.
     * @return La vista raíz del diseño del fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el diseño del fragmento y asigna la instancia de binding.
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se llama inmediatamente después de que onCreateView() haya devuelto,
     * pero antes de que se haya restaurado cualquier estado guardado en la vista.
     * Aquí es donde se configuran los listeners de la vista y se inicializa la lógica.
     * @param view La vista devuelta por onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState Si no es nulo, este fragmento está siendo recreado a partir de un estado guardado previamente.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configuración de Google Sign-In Options
        // DEFAULT_SIGN_IN solicita el ID y el perfil predeterminados del usuario.
        // requestEmail() solicita la dirección de correo electrónico del usuario.
        // requestIdToken() es crucial para obtener un ID token que se puede usar para autenticar
        // con Firebase u otros backends. Debes reemplazar "YOUR_WEB_CLIENT_ID" con el ID de cliente web
        // de tu proyecto de Firebase/Google Cloud.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de tener este string en su lugar
            .build()

        // 2. Creación del GoogleSignInClient
        // Este cliente se utilizará para iniciar el flujo de inicio de sesión de Google.
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // 3. Inicialización del ActivityResultLauncher para Google Sign-In
        // Este launcher manejará el resultado de la actividad de inicio de sesión de Google.
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Si el resultado es OK, intenta obtener la cuenta de Google.
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Obtiene la cuenta de Google.
                    val account = task.getResult(ApiException::class.java)
                    // Verifica si se obtuvo un ID token.
                    val idToken = account.idToken
                    if (idToken != null) {
                        // Si el ID token existe, llama al ViewModel para autenticar con Firebase.
                        // Asume que tu ViewModel tiene un método signInWithGoogle.
                        loginViewModel.signInWithGoogle(idToken)
                    } else {
                        // Manejo de error si el ID token es nulo.
                        Toast.makeText(context, "ID Token de Google no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    // Manejo de errores de la API de Google Sign-In.
                    Toast.makeText(context, "Fallo de inicio de sesión con Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            } else {
                // El inicio de sesión de Google fue cancelado o falló por otras razones.
                Toast.makeText(context, "Inicio de sesión con Google cancelado o fallido.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Listeners de botones ---

        // Acción para el botón de Registrar Usuario
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val nombre = binding.etNombre.text.toString()

            // Validación básica de campos
            if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
                Toast.makeText(context, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Sale del listener si los campos están vacíos
            }

            val nuevoUsuario = User(
                nombre = nombre,
                email = email
            )

            // Lanza una corrutina en el ámbito del ciclo de vida de la vista.
            // Esto asegura que la operación se cancela si la vista se destruye.
            viewLifecycleOwner.lifecycleScope.launch {
                val registrado = loginViewModel.userRepository.registerUser(email, password, nuevoUsuario)
                if (registrado) {
                    Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    // Navega al fragmento del mapa tras un registro exitoso.
                    findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
                } else {
                    Toast.makeText(context, "Error en el registro. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Acción para el botón de Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Validación básica de campos
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Por favor, introduce tu email y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Sale del listener si los campos están vacíos
            }

            // Llama al método de login en el ViewModel.
            loginViewModel.login(email, password)
        }

        // Acción para el botón de Google Sign-In
        binding.btnGoogleSignIn.setOnClickListener {
            // Inicia el flujo de inicio de sesión de Google.
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // Observador para el resultado del login (tanto para email/password como para Google).
        // Se activa cuando el ViewModel actualiza el estado de login.
        loginViewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                // Navega al fragmento del mapa tras un login exitoso.
                findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
            } else {
                Toast.makeText(context, "Error de inicio de sesión. Verifica tus credenciales.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observador para el resultado del inicio de sesión con Google
        loginViewModel.googleSignInResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
            } else {
                Toast.makeText(context, "Error al iniciar sesión con Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Se llama cuando la vista del fragmento está siendo destruida.
     * Se utiliza para limpiar la referencia al binding y evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
