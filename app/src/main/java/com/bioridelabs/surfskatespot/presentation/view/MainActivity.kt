package com.bioridelabs.surfskatespot.presentation.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.ActivityMainBinding
import com.bioridelabs.surfskatespot.di.AuthManager
import androidx.activity.OnBackPressedCallback
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject // Inyecta el AuthManager
    lateinit var authManager: AuthManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)

        // Aplicar el idioma y el tema de alto contraste guardados antes de setContentView
        applySavedPreferences()

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContentContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapFragment,
                R.id.spotListFragment,
                R.id.favoritesFragment,
                R.id.profileFragment,
                R.id.aboutUsFragment,
                R.id.addSpotFragment,
                R.id.loginFragment,
                R.id.settingsFragment // Asegúrate de que este ID existe en nav_graph.xml
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.navViewDrawer.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isUserLoggedIn = authManager.isUserLoggedIn() // Ahora esto debería funcionar

            when (destination.id) {
                R.id.loginFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar?.hide()
                    binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    supportActionBar?.show()
                    binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
            // --- LÓGICA DE RESTRICCIÓN PARA INVITADOS ---
            // Menú lateral (Drawer)
            val drawerMenu = binding.navViewDrawer.menu
            drawerMenu.findItem(R.id.addSpotFragment).isVisible = isUserLoggedIn

            // Menú de navegación inferior (Bottom Nav)
            val bottomMenu = binding.bottomNavigationView.menu
            bottomMenu.findItem(R.id.favoritesFragment).isVisible = isUserLoggedIn

            // Si el usuario es un invitado y está en la pestaña de favoritos (porque acaba de cerrar sesión),
            // lo redirigimos al mapa para evitar que se quede en una pantalla vacía.
            if (!isUserLoggedIn && destination.id == R.id.favoritesFragment) {
                navController.navigate(R.id.mapFragment)
            }


        }

        // Lógica para controlar el botón "atrás"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Si el destino actual es el MapFragment, sal de la app.
                if (navController.currentDestination?.id == R.id.mapFragment) {
                    finish()
                } else {
                    // Si no, usa el comportamiento por defecto (navegar hacia atrás en la pila)
                    // o si estamos en una pantalla de primer nivel, vuelve al mapa.
                    if (appBarConfiguration.topLevelDestinations.contains(navController.currentDestination?.id)) {
                        navController.navigate(R.id.mapFragment)
                    } else {
                        navController.navigateUp()
                    }
                }
            }
        })
    }



    private fun applySavedPreferences() {
        // Aplicar idioma guardado
        val savedLangCode = sharedPreferences.getString(SettingsFragment.KEY_LANGUAGE, "es")
        if (savedLangCode != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeList = LocaleListCompat.forLanguageTags(savedLangCode)
                AppCompatDelegate.setApplicationLocales(localeList)
            } else {
                // Para versiones anteriores, necesitas configurar la configuración antes de que se infle cualquier vista.
                // Esto es más complejo y a menudo requiere una Activity de "Wrapper" o reiniciar el proceso.
                // La solución de 'recreate()' en SettingsFragment es la más práctica para cambios en vivo.
                // Aquí solo nos aseguramos de que el Locale esté establecido al inicio.
                val locale = Locale(savedLangCode)
                Locale.setDefault(locale)
                val configuration = resources.configuration
                configuration.setLocale(locale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        }

        // Aplicar tema de alto contraste guardado
        val isHighContrastEnabled = sharedPreferences.getBoolean(SettingsFragment.KEY_HIGH_CONTRAST, false)
        if (isHighContrastEnabled) {
            setTheme(R.style.Theme_SurfSkateSpot_HighContrast)
        } else {
            setTheme(R.style.Theme_SurfSkateSpot)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}