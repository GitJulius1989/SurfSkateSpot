package com.bioridelabs.surfskatespot.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bioridelabs.surfskatespot.R
import com.bioridelabs.surfskatespot.presentation.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private lateinit var layerToggleButton: FloatingActionButton
    private var isSatelliteView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        layerToggleButton = view.findViewById(R.id.mapTypeFab)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            setupMap()
            setupLayerToggleButton() // Configurar el listener del botón aquí
        }
    }

    private fun setupMap() {
        map?.let { googleMap ->
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.isMyLocationEnabled = true
                    getUserLocation()
                } catch (securityException: SecurityException) {
                    // Manejar la excepción si el permiso fue revocado justo antes de la llamada
                    Toast.makeText(context, "Permiso de ubicación denegado por el sistema", Toast.LENGTH_SHORT).show()
                    // Podrías desactivar cualquier funcionalidad dependiente de la ubicación aquí
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }

            // Cargar spots en el mapa
            mainViewModel.spots.observe(viewLifecycleOwner) { spots ->
                spots.forEach { spot ->
                    val location = LatLng(spot.latitud, spot.longitud)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(spot.nombre)
                            .snippet(spot.descripcion)
                    )
                }
            }
        }
    }
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), // Usa requireContext() para obtener el Context de la Activity
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), // Usa requireContext() para obtener el Context de la Activity
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap() // Si el usuario da permisos, vuelve a configurar el mapa
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}