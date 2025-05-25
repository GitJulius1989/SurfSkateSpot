package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.bioridelabs.surfskatespot.R
import com.google.android.material.tabs.TabLayout

class MainContainerFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayoutMain)

        // Obtén el NavController del NavHostFragment hijo
        val navHostFragment = childFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Añadir las pestañas programáticamente con contentDescription
        tabLayout.addTab(tabLayout.newTab().setText("Mapa").apply {
            contentDescription = "Pestaña del Mapa"
        })
        tabLayout.addTab(tabLayout.newTab().setText("Perfil").apply {
            contentDescription = "Pestaña del Perfil"
        })
        tabLayout.addTab(tabLayout.newTab().setText("Sobre Nosotros").apply {
            contentDescription = "Pestaña de Sobre Nosotros"
        })

        // Configura el TabLayout para navegar con el NavController
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> navController.navigate(R.id.mapFragment)
                    1 -> navController.navigate(R.id.profileFragment)
                    2 -> navController.navigate(R.id.aboutUsFragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No es necesario implementar nada aquí por ahora
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Opcional: podrías implementar una acción al re-seleccionar la pestaña actual
            }
        })

        // Asegúrate de que la pestaña inicial seleccionada coincida con el destino de inicio del NavHostFragment hijo
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapFragment -> if (tabLayout.selectedTabPosition != 0) tabLayout.selectTab(tabLayout.getTabAt(0))
                R.id.profileFragment -> if (tabLayout.selectedTabPosition != 1) tabLayout.selectTab(tabLayout.getTabAt(1))
                R.id.aboutUsFragment -> if (tabLayout.selectedTabPosition != 2) tabLayout.selectTab(tabLayout.getTabAt(2))
            }
        }
    }
}