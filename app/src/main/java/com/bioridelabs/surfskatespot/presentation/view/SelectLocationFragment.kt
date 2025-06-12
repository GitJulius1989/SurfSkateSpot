package com.bioridelabs.surfskatespot.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.databinding.FragmentSelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SelectLocationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentSelectLocationBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null
    private var selectedLatLng: LatLng? = null

    // Claves para la comunicación entre fragmentos. Es una excelente práctica tenerlas en un companion object.
    companion object {
        const val REQUEST_KEY = "location_request"
        const val BUNDLE_KEY_LATITUDE = "latitude"
        const val BUNDLE_KEY_LONGITUDE = "longitude"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSelectLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Ubicación inicial por defecto (Santander, una buena elección 🌊)
        val santander = LatLng(43.4623, -3.8099)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(santander, 12f))

        // Listener para la pulsación larga: la lógica que querías restaurar.
        map?.setOnMapLongClickListener { latLng ->
            selectedLatLng = latLng
            map?.clear() // Limpiamos marcadores anteriores
            map?.addMarker(MarkerOptions().position(latLng).title("Ubicación Seleccionada"))
            binding.fabConfirmLocation.isVisible = true // Mostramos el botón para confirmar
        }

        // Listener para el FAB de confirmación.
        binding.fabConfirmLocation.setOnClickListener {
            selectedLatLng?.let { location ->
                // Usamos setFragmentResult para enviar los datos de vuelta a AddSpotFragment.
                setFragmentResult(REQUEST_KEY, bundleOf(
                    BUNDLE_KEY_LATITUDE to location.latitude,
                    BUNDLE_KEY_LONGITUDE to location.longitude
                ))
                findNavController().popBackStack() // Cerramos este fragmento y volvemos al anterior.
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}