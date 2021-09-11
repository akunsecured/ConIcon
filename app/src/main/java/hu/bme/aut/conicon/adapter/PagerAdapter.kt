package hu.bme.aut.conicon.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import hu.bme.aut.conicon.ui.main.home.HomeFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment

/**
 * This class is responsible for handling the ViewPager of the MainFragment
 */
class PagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment()
            }

            else -> {
                ProfileFragment()
            }
        }
    }

}