// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/MainActivity.kt
package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.View // Importa View para View.GONE/VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI // Necesario para NavigationUI.onNavDestinationSelected y navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.ActivityMainBinding // ¡IMPORTANTE! Importa el binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Declara la instancia de binding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen() // Mostrar SplashScreen
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilitar edge-to-edge

        // Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicar padding para edge-to-edge al contenido principal.
        // Ahora usamos binding.mainContentContainer directamente.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContentContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configuración de la Navegación ---

        // 1. Configurar el MaterialToolbar como ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar) // Usamos findViewById directamente

        setSupportActionBar(toolbar) // <-- ¡CAMBIO CLAVE AQUÍ! Usar la variable 'toolbar'

        // 2. Obtener el NavController del NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Configurar AppBarConfiguration para Navigation Component.
        // Define los destinos de nivel superior (top-level destinations).
        // El icono de hamburguesa se mostrará para estos; para otros, será una flecha "Up".
        // Los IDs aquí deben ser los IDs REALES de tus destinos de nivel superior en nav_graph.xml
        // que son accesibles directamente desde BottomNavigationView o NavigationDrawer.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapFragment,
                R.id.spotListFragment,
                R.id.favoritesFragment, // De Bottom Nav
                R.id.profileFragment,   // De Drawer
                R.id.aboutUsFragment,   // De Drawer
                R.id.addSpotFragment,   // De Drawer
                R.id.loginFragment      // También un destino de nivel superior, sin botón de atrás.
            ),
            binding.drawerLayout // Pasa tu DrawerLayout aquí para integrar el botón de hamburguesa
        )

        // 4. Conectar la AppBar (Toolbar) con el NavController
        // Esto mostrará el título del fragment actual en la Toolbar y el icono de hamburguesa/flecha atrás.
        // También manejará los clics en esos iconos para abrir el drawer o navegar hacia atrás.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 5. Conectar BottomNavigationView con el NavController
        // Permite que al pulsar un ítem del BottomNav se navegue al destino correspondiente
        // (si el ID del ítem de menú coincide con un ID de destino en el nav_graph).
        binding.bottomNavigationView.setupWithNavController(navController)

        // 6. Conectar NavigationView (el panel del Drawer) con el NavController
        // Permite que al pulsar un ítem del drawer se navegue al destino correspondiente.
        // Los IDs de los ítems del menú del drawer deben coincidir con los IDs de los destinos en nav_graph.xml.
        binding.navViewDrawer.setupWithNavController(navController)

        // 7. Listener para controlar la visibilidad de la BottomNavigationView y la Toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> { // Y si tienes un registro: R.id.registerFragment
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
        }
    }


    // Sobrescribir onSupportNavigateUp para que el botón de "atrás" en la Toolbar
    // y el icono de hamburguesa funcionen correctamente con el NavController y el DrawerLayout.
    override fun onSupportNavigateUp(): Boolean {
        // NavigationUI.navigateUp es el método preferido para manejar el botón de retroceso/arriba
        // con AppBarConfiguration y DrawerLayout.
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}