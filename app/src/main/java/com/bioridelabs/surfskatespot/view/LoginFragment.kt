package com.bioridelabs.surfskatespot.view

import com.bioridelabs.surfskatespot.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bioridelabs.surfskatespot.databinding.FragmentLoginBinding
import com.bioridelabs.surfskatespot.model.User
import com.bioridelabs.surfskatespot.viewmodel.LoginViewModel
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acción botón Registrar Usuario
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val nombre = binding.etNombre.text.toString()

            val nuevoUsuario = User(
                nombre = nombre,
                email = email
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val registrado = loginViewModel.userRepository.registerUser(email, password, nuevoUsuario)
                if (registrado) {
                    Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
                } else {
                    Toast.makeText(context, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            loginViewModel.login(email, password)
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
            } else {
                Toast.makeText(context, "Error de login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
