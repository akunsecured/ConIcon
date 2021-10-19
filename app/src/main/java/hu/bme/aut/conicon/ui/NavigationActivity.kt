package hu.bme.aut.conicon.ui

import android.content.Context
import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.conicon.ui.chat.ChatFragment
import hu.bme.aut.conicon.ui.login.LoginFragment
import hu.bme.aut.conicon.ui.main.MainFragment
import hu.bme.aut.conicon.ui.setusername.SetUsernameFragment

/**
 * This Activity is responsible for the navigation
 */
class NavigationActivity : SimpleNavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val auth = FirebaseAuth.getInstance()
            navigator.run {
                val sharedPref = getSharedPreferences("CONICON_AUTH", Context.MODE_PRIVATE)
                val noUsername = sharedPref.getBoolean("no_username", false)

                // Checking if we are logged in or not
                if (auth.currentUser != null) {
                    if (!noUsername) {
                        val intent = intent
                        val conversationID = intent.getStringExtra("conversationID")
                        val senderID = intent.getStringExtra("senderID")
                        // Checking if the user tapped on a chat notification
                        if (conversationID != null && senderID != null) {
                            navigator.setStack(
                                MainFragment(),
                                ChatFragment(conversationID, senderID)
                            )
                        } else {
                            navigator.add(MainFragment())
                        }
                    } else {
                        navigator.add(SetUsernameFragment())
                    }
                }
                else {
                    navigator.add(LoginFragment())
                }
            }
        }
    }
}