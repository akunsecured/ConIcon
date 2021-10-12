package hu.bme.aut.conicon.ui.main.postupload

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import com.canhub.cropper.*
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentPostuploadBinding

class PostUploadFragment : RainbowCakeFragment<PostUploadViewState, PostUploadViewModel>() {

    private lateinit var binding: FragmentPostuploadBinding
    private var filePath: Uri? = null

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
                if (!binding.tietPostDetails.text.isNullOrEmpty()) {
                    val postDetails = binding.tietPostDetails.text.toString()
                    viewModel.uploadImage(filePath!!, postDetails)
                } else {
                    viewModel.uploadImage(filePath!!)
                }
                binding.ivSend.isEnabled = false
            }
        }
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                filePath = result?.uriContent
                binding.ivSelectedImage.setImageURI(filePath)
                binding.ivSend.visibility = View.VISIBLE
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val ex = result?.error
            }
        }
    }
}