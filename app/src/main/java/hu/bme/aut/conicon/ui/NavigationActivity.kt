package hu.bme.aut.conicon.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.UserStatusListener
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.chat.ChatFragment
import hu.bme.aut.conicon.ui.login.LoginFragment
import hu.bme.aut.conicon.ui.main.MainFragment
import hu.bme.aut.conicon.ui.main.profile.ProfileFragment
import hu.bme.aut.conicon.ui.post.PostFragment
import hu.bme.aut.conicon.ui.setusername.SetUsernameFragment
import kotlinx.android.synthetic.main.fragment_post_upload_map.*

/**
 * This Activity is responsible for the navigation
 */
class NavigationActivity : SimpleNavActivity(), UserStatusListener {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var statusListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            navigator.run {
                val sharedPref = getSharedPreferences("CONICON_AUTH", Context.MODE_PRIVATE)
                val noUsername = sharedPref.getBoolean("no_username", false)

                // Checking if we are logged in or not
                if (auth.currentUser != null) {
                    if (!noUsername) {
                        val intent = intent

                        val typeValue = intent.getIntExtra("type", -1)
                        // Checking if the user tapped on a notification
                        if (typeValue != -1) {
                            when (NotificationType.getByValue(typeValue)) {
                                NotificationType.MESSAGE -> {
                                    val conversationID = intent.getStringExtra("conversationID")
                                    val senderID = intent.getStringExtra("senderID")

                                    navigator.setStack(
                                        MainFragment(),
                                        ChatFragment(conversationID!!, senderID!!)
                                    )
                                }

                                NotificationType.IMAGE_LIKE -> {
                                    val mediaID = intent.getStringExtra("mediaID")

                                    val postCollection = FirebaseFirestore.getInstance().collection("posts")
                                    postCollection.document(mediaID!!).get().addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val mediaElement = document.toObject(MediaElement::class.java)!!

                                            navigator.setStack(
                                                MainFragment(),
                                                PostFragment(mediaElement)
                                            )
                                        } else {
                                            navigator.setStack(
                                                MainFragment()
                                            )
                                        }
                                    }.addOnFailureListener {
                                        navigator.setStack(
                                            MainFragment()
                                        )
                                    }
                                }

                                NotificationType.FOLLOW -> {
                                    val receiverID = intent.getStringExtra("receiverID")

                                    navigator.setStack(
                                        MainFragment(),
                                        ProfileFragment(receiverID!!)
                                    )
                                }

                                else -> {
                                    navigator.setStack(
                                        MainFragment()
                                    )
                                }
                            }
                        } else {
                            navigator.add(MainFragment())
                        }
                    } else {
                        navigator.add(SetUsernameFragment())
                    }
                }
                else {
                    navigator.add(LoginFragment(this@NavigationActivity))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (auth.currentUser != null) {
            startListeningStatus()
        }
    }

    override fun startListeningStatus() {
        val uid = auth.currentUser?.uid.toString()

        val userRef =
            FirebaseFirestore.getInstance().collection("users").document(uid)

        statusListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                showErrorDialog(
                    getString(R.string.error_user_deleted)
                )
            }
        }
    }

    override fun stopListeningStatus() {
        statusListener.remove()
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.error_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val tvErrorMessage = dialog.findViewById<TextView>(R.id.tvErrorMessage)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        tvErrorMessage.text = message

        btnOk.setOnClickListener {
            auth.signOut()

            navigator.setStack(
                LoginFragment(this)
            )
            dialog.dismiss()
        }

        statusListener.remove()

        dialog.show()
    }
}