package com.bioridelabs.surfskatespot.presentation.view

import android.app.AlertDialog
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
import dagger.hilt.android.AndroidEntryPoint
import android.widget.RatingBar
import androidx.navigation.fragment.findNavController
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.di.AuthManager
import com.bioridelabs.surfskatespot.presentation.view.adapter.ImageSliderAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale
import javax.inject.Inject
import android.util.Log
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import androidx.lifecycle.lifecycleScope




// Fragmento para mostrar los detalles de un spot específico.
@AndroidEntryPoint
class SpotDetailFragment : Fragment() {

    @Inject // Inyecta el AuthManager
    lateinit var authManager: AuthManager

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
        currentSpotId = spotId // Guardo el ID para usarlo en los listeners

        spotDetailViewModel.loadSpotDetails(spotId) // Llama al ViewModel para cargar el spot

        // --- LÓGICA DE RESTRICCIÓN PARA INVITADOS ---
        val isUserLoggedIn = authManager.isUserLoggedIn()

        // Oculta/muestra el checkbox de favorito
        binding.llFavoriteStatus.visibility = if (isUserLoggedIn) View.VISIBLE else View.GONE

        // Oculta/muestra el botón para añadir valoración
        binding.btnAddRating.visibility = if (isUserLoggedIn) View.VISIBLE else View.GONE

        // Observa los datos del spot desde el ViewModel
        spotDetailViewModel.spotDetails.observe(viewLifecycleOwner) { spot ->
            spot?.let {
                // Ahora 'it' es de tipo 'Spot', y tiene 'nombre' y 'descripcion'
                binding.tvSpotName.text = it.nombre         // <-- ¡Aquí se resuelve!
                binding.tvSpotDescription.text = it.descripcion // <-- ¡Aquí se resuelve!

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

        observeViewModel()
        setupListeners()

    }

    private fun observeViewModel() {
        // Observador para los detalles del spot
        spotDetailViewModel.spotDetails.observe(viewLifecycleOwner) { spot ->
            // Usamos .let para ejecutar el bloque solo si spot no es nulo
            spot?.let { currentSpot ->
                // Guardamos el ID del spot actual para usarlo en los listeners
                currentSpotId = currentSpot.spotId

                // Rellenar datos básicos
                // Rellenar datos básicos de la UI
                binding.tvSpotName.text = currentSpot.nombre
                binding.tvSpotDescription.text = currentSpot.descripcion
                binding.llSportTypes.removeAllViews() // Limpiamos por si acaso
                val sportTypesText = currentSpot.tiposDeporte.joinToString(", ")
                binding.llSportTypes.addView(android.widget.TextView(context).apply { text = sportTypesText })



                // 1. Configurar la galería de imágenes
                if (currentSpot.fotosUrls.isNotEmpty()) {
                    // Hacemos visibles el slider y el indicador
                    binding.imageSlider.visibility = View.VISIBLE
                    binding.tabLayoutIndicator.visibility = View.VISIBLE

                    // Creamos y asignamos el adaptador
                    val imageAdapter = ImageSliderAdapter(currentSpot.fotosUrls)
                    binding.imageSlider.adapter = imageAdapter

                    // 2. Conectar el TabLayout con el ViewPager2 para los indicadores
                    TabLayoutMediator(binding.tabLayoutIndicator, binding.imageSlider) { tab, position ->
                        // No necesitamos hacer nada aquí, el background drawable se encarga de todo.
                    }.attach()

                } else {
                    // Si no hay fotos, ocultamos el slider y el indicador
                    binding.imageSlider.visibility = View.GONE
                    binding.tabLayoutIndicator.visibility = View.GONE
                }

                // Generar dinámicamente los tipos de deporte como Chips
                binding.llSportTypes.removeAllViews()
                currentSpot.tiposDeporte.forEach { sportType ->
                    val chip = Chip(requireContext()).apply { text = sportType }
                    binding.llSportTypes.addView(chip)
                }

                // Actualizar la sección de valoración
                if (currentSpot.totalRatings > 0) {
                    binding.ratingBarIndicator.visibility = View.VISIBLE
                    binding.ratingBarIndicator.rating = currentSpot.averageRating.toFloat()
                    binding.tvCurrentRating.text = String.format(
                        Locale.getDefault(),
                        "%.1f / 5 (%d valoraciones)",
                        currentSpot.averageRating,
                        currentSpot.totalRatings
                    )
                } else {
                    binding.ratingBarIndicator.visibility = View.GONE
                    binding.tvCurrentRating.text = "Aún no hay valoraciones."
                }

                // Lógica de visibilidad para botones de propietario y de usuario logueado
                val isLoggedIn = authManager.isUserLoggedIn()
                val isOwner = isLoggedIn && authManager.getCurrentUserId() == currentSpot.userId

                binding.llFavoriteStatus.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                binding.btnAddRating.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                // Ocultamos el botón de comentario porque su funcionalidad está en el diálogo de valoración
                binding.btnAddComment.visibility = View.GONE

                binding.btnEditSpot.visibility = if (isOwner) View.VISIBLE else View.GONE
                binding.btnDeleteSpot.visibility = if (isOwner) View.VISIBLE else View.GONE
            }


        }

        // Observador para el resultado de la eliminación
        spotDetailViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Spot eliminado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }.onFailure {
                Toast.makeText(context, "Error al eliminar: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observador para el resultado de añadir una valoración
        spotDetailViewModel.addRatingResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Error al enviar la valoración: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observador para el estado de favorito
        spotDetailViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // El observador SOLO se encarga de actualizar la UI. Nada más.
            binding.checkboxFavorite.isChecked = isFavorite
        }
        // Observador para saber si el usuario actual es el propietario del spot
        spotDetailViewModel.isOwner.observe(viewLifecycleOwner) { isOwner ->
            binding.btnEditSpot.visibility = if (isOwner) View.VISIBLE else View.GONE
            binding.btnDeleteSpot.visibility = if (isOwner) View.VISIBLE else View.GONE
        }

        // Observador para mensajes de error generales
        spotDetailViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                spotDetailViewModel.clearErrorMessage() // Limpiar para no mostrarlo de nuevo
            }
        }

        // Observador para el estado de carga
        spotDetailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Aquí podrías gestionar un ProgressBar si lo tuvieras
            binding.llActionButtons.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }


    }

    /**
     * Configura los listeners para los botones de acción del usuario.
     */
    private fun setupActionListeners() {
        binding.btnDeleteSpot.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Aquí irían los listeners para editar, añadir valoración, etc.
        // binding.btnEditSpot.setOnClickListener { ... }
    }

    private fun setupListeners() {
        // Listener para el checkbox de Favoritos
        binding.checkboxFavorite.setOnClickListener {
            // Simplemente notificamos al ViewModel que el usuario ha hecho clic.
            spotDetailViewModel.toggleFavoriteStatus()
        }

        // Listener para el botón de Añadir Valoración
        binding.btnAddRating.setOnClickListener {
            currentSpotId?.let { spotId ->
                showRatingDialog(spotId)
            }
        }

        binding.btnEditSpot.setOnClickListener {
            // Usamos la variable de la clase 'currentSpotId', que sí es visible aquí.
            // Le añadimos un '.let' para manejar el caso de que sea nulo de forma segura.
            currentSpotId?.let { id ->
                val action = SpotDetailFragmentDirections.actionSpotDetailFragmentToEditSpotFragment(id)
                findNavController().navigate(action)
            }
        }

        // Listener para el botón de Eliminar Spot
        binding.btnDeleteSpot.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    private fun showRatingDialog(spotId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialogRatingBar)
        val commentEditText = dialogView.findViewById<TextInputEditText>(R.id.dialogCommentEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Valora este Spot")
            .setView(dialogView)
            .setPositiveButton("Enviar") { dialog, _ ->
                val rating = ratingBar.rating.toInt()
                val comment = commentEditText.text.toString()
                if (rating > 0) {
                    spotDetailViewModel.submitRating(spotId, rating, comment)
                } else {
                    Toast.makeText(context, "Por favor, selecciona al menos una estrella.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                spotDetailViewModel.deleteCurrentSpot()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}