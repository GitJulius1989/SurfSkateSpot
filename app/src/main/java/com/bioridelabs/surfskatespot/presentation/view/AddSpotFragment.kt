// app/src/main/java/com/bioridelabs/surfskatespot/presentation/view/AddSpotFragment.kt
package com.bioridelabs.surfskatespot.presentation.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.FragmentAddSpotBinding
import com.bioridelabs.surfskatespot.domain.model.SportType
import com.bioridelabs.surfskatespot.presentation.view.adapter.AddPhotoAdapter // Crearemos este adaptador
import com.bioridelabs.surfskatespot.presentation.viewmodel.AddSpotViewModel
import com.google.android.gms.maps.model.LatLng
import com.bioridelabs.surfskatespot.utils.textChanges
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // Para StateFlow
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddSpotFragment : Fragment() {

    private var _binding: FragmentAddSpotBinding? = null
    private val binding get() = _binding!!

    private val addSpotViewModel: AddSpotViewModel by viewModels()
    private lateinit var photoAdapter: AddPhotoAdapter

    // Launcher para seleccionar imágenes de la galería
    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { addSpotViewModel.addPhotoUri(it) }
        }

    // Launcher para solicitar permisos de lectura de almacenamiento (necesario en Android 10 e inferior)
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImageLauncher.launch("image/*") // Si el permiso es concedido, abre la galería
            } else {
                Toast.makeText(requireContext(), R.string.permissions_denied_gallery, Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher para el resultado de seleccionar ubicación desde un mapa (si decides implementar un fragmento de selección de mapa)
    // De momento, solo para ilustrar la idea.
    private val pickLocationLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Aquí deberías extraer latitud y longitud del resultado
                // Ejemplo: val latitude = result.data?.getDoubleExtra("latitude", 0.0)
                // val longitude = result.data?.getDoubleExtra("longitude", 0.0)
                // if (latitude != null && longitude != null) {
                //     addSpotViewModel.onLocationSelected(latitude, longitude)
                // }
                Toast.makeText(context, "Ubicación seleccionada (simulada)", Toast.LENGTH_SHORT).show()
                addSpotViewModel.onLocationSelected(43.46, -3.81) // Simular una ubicación por ahora
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSpotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Escucha el resultado que viene de SelectLocationFragment
        parentFragmentManager.setFragmentResultListener(SelectLocationFragment.REQUEST_KEY, viewLifecycleOwner) { key, bundle ->
            // Cuando el resultado llega, actualizamos el ViewModel.
            val latitude = bundle.getDouble(SelectLocationFragment.BUNDLE_KEY_LATITUDE)
            val longitude = bundle.getDouble(SelectLocationFragment.BUNDLE_KEY_LONGITUDE)
            addSpotViewModel.onLocationSelected(latitude, longitude)
        }

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Configurar RecyclerView para fotos
        photoAdapter = AddPhotoAdapter { uri ->
            addSpotViewModel.removePhotoUri(uri) // Permitir eliminar fotos de la lista
        }
        binding.rvSelectedPhotos.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }

        // Observar las fotos seleccionadas para actualizar el adaptador
        addSpotViewModel.selectedPhotoUris.observe(viewLifecycleOwner) { uris ->
            photoAdapter.submitList(uris.toList()) // Usar toList() para DiffUtil en ListAdapter
            binding.rvSelectedPhotos.visibility = if (uris.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.rvSelectedPhotos.visibility = View.GONE // Ocultar al inicio si no hay fotos
    }

    private fun setupListeners() {
        // TextChangedListeners para campos de texto (usando StateFlows)
        viewLifecycleOwner.lifecycleScope.launch {
            binding.etSpotName.textChanges() // Necesitará una extensión textChanges() o un TextWatcher
                .collectLatest { text ->
                    addSpotViewModel.onSpotNameChanged(text.toString())
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.etSpotDescription.textChanges() // Necesitará una extensión textChanges() o un TextWatcher
                .collectLatest { text ->
                    addSpotViewModel.onSpotDescriptionChanged(text.toString())
                }
        }

        // Checkbox listeners
        binding.checkboxSurf.setOnClickListener {
            addSpotViewModel.onSportTypeSelected(SportType.SURF)
        }
        binding.checkboxSurfskate.setOnClickListener {
            addSpotViewModel.onSportTypeSelected(SportType.SURFSKATE)
        }
        binding.checkboxSkatepark.setOnClickListener {
            addSpotViewModel.onSportTypeSelected(SportType.SKATEPARK)
        }

        binding.btnSelectLocation.setOnClickListener {
            // Navegamos a nuestro fragmento dedicado, que es más limpio y específico.
            findNavController().navigate(R.id.action_addSpotFragment_to_selectLocationFragment)
        }
        // Botón Añadir Foto
        binding.btnAddPhoto.setOnClickListener {
            checkAndRequestGalleryPermissions()
        }

        // Botón Guardar Spot
        binding.btnSaveSpot.setOnClickListener {
            addSpotViewModel.saveSpot()
        }
    }

    private fun observeViewModel() {
        // Observar ubicación seleccionada para actualizar la UI
        addSpotViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                binding.tvSelectedLocation.text = "Lat: ${location.first}, Lon: ${location.second}"
            } else {
                binding.tvSelectedLocation.text = getString(R.string.location_not_selected)
            }
        }

        // Observar resultado de añadir spot
        addSpotViewModel.addSpotResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, R.string.spot_added_success, Snackbar.LENGTH_LONG).show()
                // Opcional: Navegar de vuelta al mapa o a la lista de spots
                findNavController().navigate(R.id.action_addSpotFragment_to_selectLocationFragment)
            }
        }

        // Observar mensajes de error
        addSpotViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                addSpotViewModel.clearErrorMessage() // Limpiar el error después de mostrar
            }
        }

        // Observar estado de carga
        addSpotViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Puedes mostrar un ProgressBar aquí si lo tienes en el layout
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveSpot.isEnabled = !isLoading // Deshabilitar botón al cargar
        }
    }

    private fun checkAndRequestGalleryPermissions() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permissionToRequest
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Ya tienes permiso, lanza directamente el selector.
                pickImageLauncher.launch("image/*")
            }
            shouldShowRequestPermissionRationale(permissionToRequest) -> {
                // Opcional: Muestra un diálogo explicando por qué necesitas el permiso.
                Snackbar.make(binding.root, R.string.permissions_needed_gallery, Snackbar.LENGTH_LONG)
                    .setAction("OK") { requestPermissionLauncher.launch(permissionToRequest) }
                    .show()
            }
            else -> {
                // Pide el permiso por primera vez.
                requestPermissionLauncher.launch(permissionToRequest)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}