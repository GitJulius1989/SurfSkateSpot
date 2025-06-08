package com.bioridelabs.surfskatespot.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Para el botón de confirmar en el modo selección
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf // Para setFragmentResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult // Para setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs // Para los argumentos del mapa
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.domain.model.Spot
import com.bioridelabs.surfskatespot.domain.model.SportType
import com.bioridelabs.surfskatespot.presentation.viewmodel.FavoritesViewModel // Para obtener favoritos
import com.bioridelabs.surfskatespot.presentation.viewmodel.MainViewModel
import com.bioridelabs.surfskatespot.utils.BitmapHelper // Importa el BitmapHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels() // Para obtener los favoritos
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private lateinit var layerToggleButton: FloatingActionButton
    private lateinit var spotsVisibilityFab: FloatingActionButton



    private var isSatelliteView = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val args: MapFragmentArgs by navArgs() // Para acceder a los argumentos del mapa
    private var currentFavoriteSpotIds: Set<String> = emptySet() // Para guardar IDs de favoritos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        layerToggleButton = view.findViewById(R.id.mapTypeFab)
        spotsVisibilityFab = view.findViewById(R.id.spotsVisibilityFab)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
        setupLayerToggleButton()
        setupSpotsVisibilityFab()

        // Observar favoritos para actualizar los marcadores (solo si no estamos en modo selección)
        if (!args.selectionMode) {
            favoritesViewModel.favoriteSpots.observe(viewLifecycleOwner) { favoriteSpots ->
                currentFavoriteSpotIds = favoriteSpots.mapNotNull { it.spotId }.toSet()
                // Redibujar los marcadores cuando la lista de favoritos cambie
                map?.clear()
                mainViewModel.spots.value?.let { drawSpotsOnMap(it) } // Redibujar spots con el estado de favoritos actualizado
            }

            // Observar todos los spots para mostrarlos en el mapa
            mainViewModel.spots.observe(viewLifecycleOwner) { spots ->
                drawSpotsOnMap(spots)
            }
        }
    }

    private fun setupMap() {
        map?.let { googleMap ->
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.isMyLocationEnabled = true
                    getUserLocation()
                } catch (securityException: SecurityException) {
                    Toast.makeText(context, "Permiso de ubicación denegado por el sistema", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

            googleMap.setOnMarkerClickListener { marker ->
                val spotId = marker.tag as? String
                spotId?.let {
                    // Si estamos en modo selección, no navegar a detalle al hacer clic en un marcador existente
                    if (!args.selectionMode) {
                        val action = MapFragmentDirections.actionMapFragmentToSpotDetailFragment(it)
                        findNavController().navigate(action)
                    }
                }
                true
            }
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
            }
        }
    }

    private fun setupLayerToggleButton() {
        layerToggleButton.setOnClickListener {
            isSatelliteView = !isSatelliteView
            map?.mapType = if (isSatelliteView) {
                GoogleMap.MAP_TYPE_SATELLITE
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }

    //Para filtrar spots, en la v2
    private fun setupSpotsVisibilityFab() {
        spotsVisibilityFab.setOnClickListener {
            Toast.makeText(context, "Funcionalidad de filtro de spots", Toast.LENGTH_SHORT).show()
        }
    }

    // Nueva función para dibujar los spots en el mapa
    private fun drawSpotsOnMap(spots: List<Spot>) {
        map?.clear() // Limpiar marcadores existentes
        spots.forEach { spot ->
            val location = LatLng(spot.latitud, spot.longitud)
            val isFavorite = currentFavoriteSpotIds.contains(spot.spotId) // Comprobar si es favorito

            val markerOptions = MarkerOptions()
                .position(location)
                .title(spot.nombre)
                .snippet(buildMarkerSnippet(spot))
                .icon(getSpotIcon(spot, isFavorite)) // Pasar el estado de favorito
                .anchor(0.5f, 0.5f) // Centrar el icono del marcador

            val marker = map?.addMarker(markerOptions)
            marker?.tag = spot.spotId
        }
    }

    // Modificar getSpotIcon para que reciba el estado de favorito
    private fun getSpotIcon(spot: Spot, isFavorite: Boolean): BitmapDescriptor {
        // Determinar el icono base según el tipo de deporte
        val baseIconResId: Int = when {
            // Prioridad: Surfskate si se selecciona específicamente
            // Prioridad de los tipos de deporte para el icono
            spot.tiposDeporte.contains(SportType.SURF.type) -> SportType.SURF.iconResId
            spot.tiposDeporte.contains(SportType.SURFSKATE.type) -> SportType.SURFSKATE.iconResId // Nuevo orden
            spot.tiposDeporte.contains(SportType.SKATEPARK.type) -> SportType.SKATEPARK.iconResId
            else -> R.drawable.ic_spot // Un icono por defecto si no coincide con ninguno
        }
        // Determinar el color de tintado
        val tintColorResId = if (isFavorite) R.color.yellow_gold else R.color.black // Cambiar tint a yellow_gold si es favorito

        // Usar BitmapHelper para crear el BitmapDescriptor a partir del VectorDrawable
        return BitmapHelper.vectorToBitmap(requireContext(), baseIconResId, tintColorResId)
    }


    private fun buildMarkerSnippet(spot: Spot): String {
        val types = spot.tiposDeporte.joinToString(", ")
        val ratingText = if (spot.totalRatings > 0) {
            "Valoración: %.1f/5 (%d)".format(spot.averageRating, spot.totalRatings)
        } else {
            "Sin valoraciones"
        }
        return "Tipos: $types\n$ratingText\n${spot.descripcion}"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}