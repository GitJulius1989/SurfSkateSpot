package com.bioridelabs.surfskatespot.presentation.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    companion object { // <--- Así es como debe estar
        const val PREFS_NAME = "SurfSkateSpotPrefs"
        const val KEY_LANGUAGE = "language_preference"
        const val KEY_HIGH_CONTRAST = "high_contrast_preference"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupLanguageSpinner()
        setupHighContrastCheckbox()
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.languages_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        // Recuperar preferencia de idioma y seleccionarla
        val savedLangCode = sharedPreferences.getString(KEY_LANGUAGE, "es") // "es" por defecto
        val selectedPosition = when (savedLangCode) {
            "es" -> 0
            "en" -> 1
            else -> 0
        }
        binding.spinnerLanguage.setSelection(selectedPosition)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguageCode = when (position) {
                    0 -> "es" // Español
                    1 -> "en" // Inglés
                    else -> "es"
                }

                val currentLangCode = sharedPreferences.getString(KEY_LANGUAGE, "es")
                if (selectedLanguageCode != currentLangCode) {
                    // Guardar nueva preferencia de idioma
                    sharedPreferences.edit().putString(KEY_LANGUAGE, selectedLanguageCode).apply()

                    // Aplicar idioma
                    applyLanguage(selectedLanguageCode)
                    Toast.makeText(context, R.string.reinicia_para_cambios_idioma, Toast.LENGTH_LONG).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale) // Establecer el locale predeterminado

        // Actualizar la configuración de la aplicación
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Android 13+ (API 33) tiene una forma preferida para cambiar el idioma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeList = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        } else {
            // Para versiones anteriores, la recreación de la actividad es más robusta
            // Aunque updateConfiguration funciona, a veces no actualiza todas las vistas
            // Se le pide al usuario que reinicie la app
            requireActivity().recreate() // Esto recrea la actividad, aplicando el nuevo idioma
        }
    }

    private fun setupHighContrastCheckbox() {
        val isHighContrastEnabled = sharedPreferences.getBoolean(KEY_HIGH_CONTRAST, false)
        binding.checkboxHighContrast.isChecked = isHighContrastEnabled

        binding.checkboxHighContrast.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_HIGH_CONTRAST, isChecked).apply()
            // Recrear la actividad para aplicar el nuevo tema con los estilos de texto
            requireActivity().recreate()
            Toast.makeText(context, R.string.preferencias_guardadas, Toast.LENGTH_SHORT).show()
        }
    }

//    private fun applyHighContrastTheme(enable: Boolean) {
//        if (enable) {
//            requireActivity().setTheme(R.style.Theme_SurfSkateSpot_HighContrast)
//            // Para aplicar el tema a todas las vistas después del cambio
//            requireActivity().recreate()
//        } else {
//            requireActivity().setTheme(R.style.Theme_SurfSkateSpot)
//            // Para volver al tema normal
//            requireActivity().recreate()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}