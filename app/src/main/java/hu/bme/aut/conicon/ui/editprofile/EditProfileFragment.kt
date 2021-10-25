package hu.bme.aut.conicon.ui.editprofile

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.databinding.FragmentEditprofileBinding
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.CommonMethods
import hu.bme.aut.conicon.ui.login.LoginFragment
import java.util.*

class EditProfileFragment(private val user: AppUser) : RainbowCakeFragment<EditProfileViewState, EditProfileViewModel>() {

    companion object {
        private const val SELECT_IMAGE = 123
    }

    private lateinit var binding: FragmentEditprofileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditprofileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (user.photoUrl != null) {
            Picasso.get().load(user.photoUrl).into(binding.ivProfilePicture)
        }

        binding.tvEditProfilePicture.setOnClickListener {
            selectImage()
        }

        binding.tietUsername.addTextChangedListener {
            binding.ivAccept.visibility = View.GONE

            if (CommonMethods().validateEditText(
                            requireContext(),
                            binding.tietUsername,
                            binding.tilUsername,
                            6, 20,
                            CommonMethods().regexUsername
                    )) {
                binding.ivAccept.visibility = View.VISIBLE
            }
        }

        binding.ivCancel.setOnClickListener {
            navigator?.pop()
        }

        binding.ivAccept.setOnClickListener {
            viewModel.updateUsername(binding.tietUsername.text.toString().toLowerCase(Locale.ROOT))
        }

        binding.btnDeleteProfile.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
                    .setMessage("Do you want to delete the profile?")
                    .setPositiveButton("Yes") { _, _ -> viewModel.deleteProfile(user) }
                    .setNegativeButton("No") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), 123)
    }

    override fun getViewResource(): Int = R.layout.fragment_editprofile

    override fun provideViewModel(): EditProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: EditProfileViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProgressBar.visibility = View.GONE
            }

            Loading -> {
                binding.pbProgressBar.visibility = View.VISIBLE
            }

            SuccessfullyUpdated -> {
                viewModel.init()
            }

            is FirebaseError -> {
                viewModel.init()
            }

            UsernameTakenError -> {
                CommonMethods().showEditTextError(requireContext(), binding.tilUsername, "Username is already taken!")
                viewModel.init()
            }

            UploadReady -> {
                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                viewModel.init()
            }

            ProfileDeleted -> {
                viewModel.init()
                navigator?.run {
                    setStack()
                    add(LoginFragment())
                    executePending()
                }
            }
        }.exhaustive
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                if (data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                        viewModel.updateProfilePicture(imageUri)
                    }
                }
            }
        }
    }
}