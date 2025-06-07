// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/SpotDetailFragment.kt
package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bioridelabs.surfskatespot.databinding.FragmentSpotDetailBinding
import com.bioridelabs.surfskatespot.presentation.viewmodel.SpotDetailViewModel
import com.google.android.material.snackbar.Snackbar

// Fragmento para mostrar los detalles de un spot específico.
class SpotDetailFragment : Fragment() {

    private var _binding: FragmentSpotDetailBinding? = null
    private val binding get() = _binding!!

    private val spotDetailViewModel: SpotDetailViewModel by viewModels()

    // Para recibir los argumentos del spot (ej. el ID del spot)
    // Asegúrate de que el argumento "spotId" está definido en tu nav_graph.xml
    private val args: SpotDetailFragmentArgs by navArgs() // Descomentar esta línea

    private var currentSpotId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpotDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtén el ID del spot de los argumentos de navegación
        val spotId = args.spotId // Ahora puedes usar 'args.spotId'
        spotDetailViewModel.loadSpotDetails(spotId) // Llama al ViewModel para cargar el spot

        // Observa los datos del spot desde el ViewModel
        spotDetailViewModel.spotDetails.observe(viewLifecycleOwner) { spot ->
            spot?.let {
                // Ahora 'it' es de tipo 'Spot', y tiene 'nombre' y 'descripcion'
                binding.tvSpotName.text = it.nombre         // <-- ¡Aquí se resuelve!
                binding.tvSpotDescription.text = it.descripcion // <-- ¡Aquí se resuelve!

                // Si usas la versión con 'tipo' y 'fotoUrl' (tu modelo actual)
                // binding.tvSportTypeLabel.text = "Tipo de deporte: ${it.tipo}"
                // if (it.fotoUrl != null) {
                //     // Cargar imagen con Glide o Coil
                //     // Glide.with(this).load(it.fotoUrl).into(binding.imageViewSpot)
                // }

                // Si usas la versión mejorada con 'tiposDeporte' y 'fotosUrls'
                // binding.llSportTypes.removeAllViews() // Limpia vistas anteriores
                // it.tiposDeporte.forEach { tipo ->
                //     val chip = Chip(requireContext()).apply { text = tipo }
                //     binding.llSportTypes.addView(chip)
                // }
                // Implementar el carrusel de imágenes con ViewPager2 para 'fotosUrls'

                // Aquí iría la lógica para configurar tu ViewPager2 para las imágenes
                // y tu RecyclerView para los comentarios, si los tienes.

                // Lógica de visibilidad de botones (editar/eliminar)
                // val currentUserId = loginViewModel.currentUserId // Necesitarías inyectar FirebaseAuth en LoginViewModel o tener un AuthRepository
                // if (currentUserId == it.userId) { // Si el usuario actual es el creador
                //    binding.btnEditSpot.visibility = View.VISIBLE
                //    binding.btnDeleteSpot.visibility = View.VISIBLE
                // } else {
                //    binding.btnEditSpot.visibility = View.GONE
                //    binding.btnDeleteSpot.visibility = View.GONE
                // }
            } ?: run {
                // Si el spot es nulo (no encontrado o error)
                Toast.makeText(context, "Spot no encontrado o error al cargar.", Toast.LENGTH_LONG).show()
                // Puedes navegar de vuelta o mostrar un mensaje de error
            }
        }

        // Observar errores desde el ViewModel
        spotDetailViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observar estado de carga
        spotDetailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Aquí puedes mostrar/ocultar un ProgressBar
        }

        // Aquí configurarías los listeners para los botones de acción
        // binding.btnEditSpot.setOnClickListener { /* Navegar a la pantalla de edición */ }
        // binding.btnDeleteSpot.setOnClickListener { /* Mostrar diálogo de confirmación y eliminar */ }
        // binding.btnAddComment.setOnClickListener { /* Abrir diálogo para añadir comentario */ }
        // binding.btnAddRating.setOnClickListener { /* Abrir diálogo para añadir valoración */ }
    }

    private fun observeViewModel() {
        spotDetailViewModel.spotDetails.observe(viewLifecycleOwner) { spot ->
            spot?.let {
                binding.tvSpotName.text = it.nombre
                binding.tvSpotDescription.text = it.descripcion

                // Aquí iría la lógica para configurar tu ViewPager2 para las imágenes
                // y tu RecyclerView para los comentarios, si los tienes.

                // Lógica de visibilidad de botones (editar/eliminar)
                // Implementar después de refactorizar UserRepository y obtener el UID actual.
                // val currentUserId = firebaseAuth.currentUser?.uid // Si lo tienes directamente en el fragment
                // if (currentUserId == it.userId) {
                //    binding.btnEditSpot.visibility = View.VISIBLE
                //    binding.btnDeleteSpot.visibility = View.VISIBLE
                // } else {
                //    binding.btnEditSpot.visibility = View.GONE
                //    binding.btnDeleteSpot.visibility = View.GONE
                // }
            } ?: run {
                Toast.makeText(context, "Spot no encontrado o error al cargar.", Toast.LENGTH_LONG).show()
                // Puedes navegar de vuelta o mostrar un mensaje de error
            }
        }

        // Observar el estado de favorito
        spotDetailViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            binding.checkboxFavorite.isChecked = isFavorite
        }

        // Observar errores desde el ViewModel
        spotDetailViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Uso de SnackBar para mensajes más persistentes y con opción a acción, si quieres
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                spotDetailViewModel.clearErrorMessage() // Limpiar el error después de mostrarlo
            }
        }

        // Observar estado de carga
        spotDetailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Aquí puedes mostrar/ocultar un ProgressBar global si tu layout lo tuviera
            // Por ejemplo, si tuvieras un ProgressBar con id 'progressBar' en este layout:
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.checkboxFavorite.setOnCheckedChangeListener { _, isChecked ->
            // Cuando el usuario cambia el estado del checkbox, actualiza el ViewModel
            currentSpotId?.let { id ->
                spotDetailViewModel.toggleFavorite(id, !isChecked) // ¡Importante! currentIsFavorite es el estado antes del cambio
            }
        }
        // Aquí configurarías los listeners para los otros botones de acción
        // binding.btnEditSpot.setOnClickListener { /* Navegar a la pantalla de edición */ }
        // binding.btnDeleteSpot.setOnClickListener { /* Mostrar diálogo de confirmación y eliminar */ }
        // binding.btnAddComment.setOnClickListener { /* Abrir diálogo para añadir comentario */ }
        // binding.btnAddRating.setOnClickListener { /* Abrir diálogo para añadir valoración */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}