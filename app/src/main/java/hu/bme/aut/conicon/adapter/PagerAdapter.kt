package hu.bme.aut.conicon.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.conicon.ui.main.home.HomeFragment
import hu.bme.aut.conicon.ui.main.postupload.PostUploadFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment

/**
 * This class is responsible for handling the ViewPager of the MainFragment
 */
class PagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        val auth = FirebaseAuth.getInstance()

        val homeFragment = HomeFragment()
        val postUploadFragment = PostUploadFragment()
        val profileFragment = ProfileFragment(auth.currentUser?.uid.toString(), false)

        return when (position) {
            0 -> {
                homeFragment
            }

            1 -> {
                postUploadFragment
            }

            else -> {
                profileFragment
            }
        }
    }


}