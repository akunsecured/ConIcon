package hu.bme.aut.conicon.ui.chat_imageview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.databinding.FragmentImageViewBinding

class ImageViewFragment(private val mediaLink: String) : Fragment() {

    private lateinit var binding: FragmentImageViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentImageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.get().load(mediaLink).into(binding.ivPicture)
    }
}