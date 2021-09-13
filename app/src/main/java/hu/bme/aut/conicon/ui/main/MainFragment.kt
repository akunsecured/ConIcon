package hu.bme.aut.conicon.ui.main

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager2.widget.ViewPager2
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.adapter.PagerAdapter
import hu.bme.aut.conicon.databinding.FragmentMainBinding

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
            R.drawable.ic_add,
            R.drawable.ic_profile
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkOnlineStatus()
        checkIfEmailIsVerified()

        setupTabLayout(binding.tlTabLayout, binding.vpViewPager)
    }

    override fun onResume() {
        super.onResume()

        checkIfEmailIsVerified()
    }

    /**
     * This method is responsible for checking the online status of the user.
     * If the app has lost the connection to the Firebase Database server,
     * a red TextView will appear at the top of the View. This TextView will
     * disappear as soon as the user has got internet connection again.
     */
    private fun checkOnlineStatus() {
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                val tvConnectionInfo = binding.tvConnectionInfo

                val alpha: Float = if (connected) 0f else 1f

                if (!connected) {
                    tvConnectionInfo.alpha = 0f
                    tvConnectionInfo.visibility = View.VISIBLE
                }

                // Showing connection info
                tvConnectionInfo.animate().setDuration(2000).alpha(alpha).setListener(object: Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        // It can be empty
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (connected) {
                            tvConnectionInfo.visibility = View.GONE
                        }
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        // It can be empty
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        // It can be empty
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Listener was cancelled
                Log.d("FirebaseDatabase", "Online checking listener was cancelled")
            }

        })
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
        tabLayout.addTab(tabLayout.newTab().setTag("ADD"))
        tabLayout.addTab(tabLayout.newTab().setTag("PROFILE"))

        viewPager.adapter = PagerAdapter(requireActivity(), tabLayout.tabCount)
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = AppCompatResources.getDrawable(requireContext(), tabIcons[position])
        }.attach()
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