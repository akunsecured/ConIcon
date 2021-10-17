package hu.bme.aut.conicon.ui.postupload_map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GpsStatus.GPS_EVENT_STARTED
import android.location.GpsStatus.GPS_EVENT_STOPPED
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import hu.bme.aut.conicon.R

class PostUploadMapFragment : Fragment() {

    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var btnUseCurrentLocation: Button
    private lateinit var btnSelectLocation: Button
    private var selectedLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        gMap = googleMap
        gMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        getCurrentLocation()
        gMap.isMyLocationEnabled = true
        gMap.uiSettings.isZoomControlsEnabled = false
        gMap.uiSettings.isMapToolbarEnabled = false
        gMap.setOnMapClickListener { position ->
            placeMarker(position)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post_upload_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        btnUseCurrentLocation = view.findViewById(R.id.btnUseCurrentLocation)
        btnSelectLocation = view.findViewById(R.id.btnSelectLocation)

        btnUseCurrentLocation.setOnClickListener {
            placeMarker(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!))
        }

        btnSelectLocation.setOnClickListener {
            if (selectedLocation != null) {
                val intent = Intent()
                intent.putExtra("lat", selectedLocation!!.latitude)
                intent.putExtra("lng", selectedLocation!!.longitude)
                targetFragment?.onActivityResult(11111, Activity.RESULT_OK, intent)
                navigator?.pop()
            }
        }
    }

    private fun placeMarker(position: LatLng) {
        selectedLocation = position
        gMap.clear()
        gMap.addMarker(
                MarkerOptions().position(position)
        )
        btnSelectLocation.visibility = View.VISIBLE
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

            if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                requireActivity().requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        101)
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        currentLocation = task.result as Location
                        if (currentLocation != null) {
                            btnUseCurrentLocation.visibility = View.VISIBLE

                            gMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!), 16.0f
                                    )
                            )
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            }
        }
    }
}