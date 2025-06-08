package com.bioridelabs.surfskatespot.presentation.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bioridelabs.surfskatespot.R // Asegúrate de importar correctamente R

import android.content.Intent // Para la navegación
import android.os.Handler
import android.os.Looper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SplashActivity : AppCompatActivity() {

    private val splashDelay: Long = 2000 // Duración total de la splash screen en ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultar barras de sistema para una experiencia de pantalla completa
        hideSystemBars()
        setContentView(R.layout.activity_splash)

        // Asegurarse de que el logo está en la posición inicial (fuera de la pantalla, arriba)
        val logoImageView = findViewById<View>(R.id.imageViewLogo)
        logoImageView.translationY = -resources.displayMetrics.heightPixels.toFloat() // Lo movemos por encima de la pantalla

        // Iniciar la animación
        startSplashAnimation(logoImageView)

        // Navegar a MainActivity después de un delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza la SplashActivity para que no se pueda volver atrás
        }, splashDelay)
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the system bars to be hidden and true to be drawn behind your app's content.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun startSplashAnimation(view: View) {
        // Altura total de la pantalla para calcular el "suelo"
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // 1. Animación de caída: desde arriba (fuera de pantalla) hasta el centro
        val dropAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
            duration = 800 // Duración de la caída
            interpolator = AccelerateInterpolator(1.5f) // Caída acelerada (el factor controla la intensidad de la aceleración)
        }

        // 2. Animación de rebote: desde el centro hacia abajo y luego de vuelta al centro
        // Es un ValueAnimator para tener más control sobre los valores del rebote.
        val bounceAnimator = ValueAnimator.ofFloat(0f, -0.1f * view.height, 0f).apply {
            duration = 500 // Duración del rebote
            interpolator = BounceInterpolator() // Efecto de rebote
        }

        // Coordinar las animaciones
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(dropAnimator, bounceAnimator) // Primero cae, luego rebota
        animatorSet.start()
    }
}