package com.bioridelabs.surfskatespot.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bioridelabs.surfskatespot.R
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController




class MainActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Si tienes una AppBar, configura la acción de Up:
        val navController = findNavController(R.id.nav_host_fragment)
        // Configura la barra de acción con el NavController
        // Linea comentada para test
    //     setupActionBarWithNavController(navController)

    }
    // Permite que el botón de "Up" funcione correctamente:
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}