package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // DESCOMENTADO: Import para la Toolbar
import androidx.constraintlayout.widget.ConstraintLayout // Para el padding del contenido principal
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController // DESCOMENTADO: Import para conectar la ActionBar
import androidx.navigation.ui.setupWithNavController
import com.bioridelabs.surfskatespot.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Aplicar padding para edge-to-edge al contenedor principal del contenido
        val mainContentContainer = findViewById<ConstraintLayout>(R.id.main_content_container)
        ViewCompat.setOnApplyWindowInsetsListener(mainContentContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configuración de la Navegación ---

        // 1. Toolbar (AHORA ACTIVA)
        // Asegúrate de que en tu activity_main.xml tienes una Toolbar con android:id="@+id/topAppBar"
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar) // Establece esta Toolbar como la ActionBar de la Activity

        // 2. NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // 4. NavigationView (el panel del drawer)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_drawer)

        // 5. AppBarConfiguration
        // Define los destinos de nivel superior. El icono de hamburguesa se mostrará para estos.
        // ¡¡¡IMPORTANTE!!! REEMPLAZA R.id.mapFragment, R.id.spotListFragment
        // CON LOS IDs REALES de tus destinos de nivel superior definidos en tu nav_graph.xml.
        // Estos son los fragmentos a los que se accede desde BottomNavigationView o NavigationDrawer.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapFragment, R.id.spotListFragment // <<-- EJEMPLO: ¡REEMPLAZA ESTOS IDs!
                // Añade aquí todos los IDs de tus fragmentos de nivel superior.
                // Por ejemplo, si tienes un fragmento para "Lista de Spots" accesible
                // desde el BottomNav, su ID debería estar aquí. Si "Perfil" es un destino
                // de nivel superior accesible desde el Drawer, su ID también iría aquí.
            ),
            drawerLayout // Pasa tu DrawerLayout aquí
        )

        // 6. Conectar Toolbar con NavController y AppBarConfiguration (AHORA ACTIVA)
        // Esto mostrará el título del fragment actual en la Toolbar y el icono de hamburguesa/flecha atrás.
        // También manejará los clics en esos iconos para abrir el drawer o navegar hacia atrás.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 7. Conectar NavigationView (Drawer) con NavController
        // Permite que al pulsar un ítem del drawer se navegue al destino correspondiente
        // (si el ID del ítem de menú coincide con un ID de destino en el nav_graph).
        navigationView.setupWithNavController(navController)

        // 8. Conectar BottomNavigationView con NavController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        // Manejar clics en items del drawer que NO sean solo para navegación (opcional pero recomendado)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Primero, intenta que NavigationUI maneje el evento.
            val navigated = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (navigated) {
                drawerLayout.closeDrawers()
                return@setNavigationItemSelectedListener true
            }

            // Si NavigationUI no lo manejó (acción custom):
            when (menuItem.itemId) {
                R.id.nav_logout -> { // Asumiendo que tienes R.id.nav_logout en drawer_menu.xml
                    // Lógica para cerrar sesión
                    // Toast.makeText(this, "Cerrar Sesión Pulsado", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                // Otros casos para acciones personalizadas
                else -> false
            }
        }
    }

    // Sobrescribir onSupportNavigateUp para que el botón de "atrás" en la Toolbar
    // y el icono de hamburguesa funcionen correctamente con el NavController y el DrawerLayout.
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}