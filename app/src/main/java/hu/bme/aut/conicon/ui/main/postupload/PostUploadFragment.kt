package hu.bme.aut.conicon.ui.main.postupload

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.canhub.cropper.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentPostuploadBinding
import hu.bme.aut.conicon.network.model.PostLocation
import hu.bme.aut.conicon.ui.postupload_map.PostUploadMapFragment
import timber.log.Timber
import java.io.IOException
import java.util.*

class PostUploadFragment : RainbowCakeFragment<PostUploadViewState, PostUploadViewModel>() {

    private lateinit var binding: FragmentPostuploadBinding
    private var filePath: Uri? = null
    private var postLocation: PostLocation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostuploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectImage.setOnClickListener {
            startCropImageActivity()
        }
        
        binding.ivSend.setOnClickListener {
            if (filePath != null) {
                var postDetails: String? = null
                if (!binding.tietPostDetails.text.isNullOrEmpty()) {
                    postDetails = binding.tietPostDetails.text.toString()
                }
                viewModel.uploadImage(filePath!!, postDetails, postLocation)
                binding.ivSend.isEnabled = false
            }
        }

        binding.btnSelectLocation.setOnClickListener {
            checkPermission()
        }

        binding.ivClearLocation.setOnClickListener {
            binding.tvLocation.text = ""
            binding.llLocation.visibility = View.GONE
            postLocation = null
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                        this.requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        this.requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
            openLocationChooser()
        } else {
            requestPermissions(
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 44
            )
        }
    }

    private fun openLocationChooser() {
        val postUploadMapFragment = PostUploadMapFragment()
        postUploadMapFragment.setTargetFragment(this, 11111)
        navigator?.add(postUploadMapFragment)
    }

    /**
     * This method will start an Activity that will help the user to choose and crop an image
     */
    private fun startCropImageActivity() {
        CropImage.activity()
            .setAspectRatio(1, 1)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(requireContext(), this)
    }

    override fun getViewResource(): Int = R.layout.fragment_postupload

    override fun provideViewModel(): PostUploadViewModel = getViewModelFromFactory()

    override fun render(viewState: PostUploadViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
                binding.btnSelectImage.isEnabled = true
                binding.ivSend.isEnabled = true
                binding.llLocation.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
                binding.btnSelectImage.isEnabled = false
            }

            UploadReady -> {
                Toast.makeText(requireContext(), "Upload ready", Toast.LENGTH_SHORT).show()
                binding.ivSend.visibility = View.GONE
                binding.tietPostDetails.text?.clear()
                binding.ivSelectedImage.setImageResource(R.drawable.ic_image)
                filePath = null
                viewModel.init()
            }

            is FirebaseError -> {
                Toast.makeText(requireContext(), viewState.message, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    filePath = result?.uriContent
                    binding.ivSelectedImage.setImageURI(filePath)
                    binding.ivSend.visibility = View.VISIBLE
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val ex = result?.error
                }
            }

            11111 -> {
                if (resultCode == RESULT_OK) {
                    val lat = data?.getDoubleExtra("lat", -1000.0)!!
                    val lng = data.getDoubleExtra("lng", -1000.0)
                    if (lat != -1000.0 && lng != -1000.0) {
                        updateLocation(LatLng(lat, lng))
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 44){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openLocationChooser()
            }
            else {
                Snackbar.make(
                        requireView(),
                        getString(R.string.location_permission_request),
                        Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateLocation(position: LatLng) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geoCoder.getFromLocation(position.latitude, position.longitude, 1)
            val locality = addresses[0].locality
            binding.tvLocation.text = locality

            postLocation = PostLocation(position.latitude, position.longitude, locality)
            binding.llLocation.visibility = View.VISIBLE
        } catch (ex: Exception) {
            Timber.d(ex.message.toString())
        }
    }
}