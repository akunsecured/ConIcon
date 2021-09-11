package hu.bme.aut.conicon.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager2.widget.ViewPager2
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.PagerAdapter
import hu.bme.aut.conicon.databinding.FragmentMainBinding
import hu.bme.aut.conicon.ui.login.LoginFragment

/**
 * This is the application's main view
 */
class MainFragment : RainbowCakeFragment<MainViewState, MainViewModel>() {

    private lateinit var binding: FragmentMainBinding

    /**
     * Icons for the TabLayout
     */
    private val tabIcons = arrayListOf (
            R.drawable.ic_home,
            R.drawable.ic_profile
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfEmailIsVerified()

        binding.btnFab.setOnClickListener {
            // TODO: Implementation of adding a new post instead of signing out
            signOut()
        }

        binding.ivLogoMin.setOnClickListener {
            // Changing the TabLayout to the HomeFragment
            binding.vpViewPager.currentItem = 0
        }

        binding.ivMessages.setOnClickListener {
            // TODO: Start the activity of the conversations
        }

        setupTabLayout(binding.tlTabLayout, binding.vpViewPager)
    }

    override fun onResume() {
        super.onResume()

        checkIfEmailIsVerified()
    }

    /**
     * This method's exercise is to check if the user has verified his email
     */
    private fun checkIfEmailIsVerified() {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnSuccessListener {
            binding.tvEmailVerification.visibility = if (FirebaseAuth.getInstance().currentUser?.isEmailVerified as Boolean) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    /**
     * This method is responsible for the setup of the view's TabLayout
     * @param tabLayout The TabLayout
     * @param viewPager The ViewPager2 object that will be connect to the given TabLayout
     */
    private fun setupTabLayout(tabLayout: TabLayout, viewPager: ViewPager2) {
        tabLayout.addTab(tabLayout.newTab().setTag("HOME"))
        tabLayout.addTab(tabLayout.newTab().setTag("PROFILE"))

        viewPager.adapter = PagerAdapter(requireActivity(), tabLayout.tabCount)
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = AppCompatResources.getDrawable(requireContext(), tabIcons[position])
        }.attach()
    }

    /**
     * Sign out method for testing
     */
    private fun signOut() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        navigator?.replace(LoginFragment(), R.anim.from_down_to_up_in, R.anim.from_down_to_up_out, R.anim.from_up_to_down_in, R.anim.from_up_to_down_out)
    }

    override fun getViewResource(): Int = R.layout.fragment_main

    override fun provideViewModel(): MainViewModel = getViewModelFromFactory()

    override fun render(viewState: MainViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}