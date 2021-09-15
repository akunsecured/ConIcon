package hu.bme.aut.conicon.ui.likes

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class UsersViewModel @Inject constructor(

) : RainbowCakeViewModel<UsersViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getUserData(ids: MutableList<String>) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userElements = mutableListOf<AppUser>()

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        if (ids.isNotEmpty()) {
            val query = userCollection.whereIn("id", ids)
            query.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val id = document.data?.get("id").toString()
                        val username = document.data?.get("username").toString()
                        val email = document.data?.get("email").toString()
                        var photoUrl: String? = document.data?.get("photoUrl") as String?
                        val followers = document.data?.get("followers") as MutableList<String>
                        val following = document.data?.get("following") as MutableList<String>
                        val posts = document.data?.get("posts") as MutableList<String>

                        userElements.add(
                                AppUser(
                                        id, username, email, photoUrl, followers, following, posts
                                )
                        )
                    }
                }

                viewState = UsersReady(userElements)
            }.addOnFailureListener { ex ->
                viewState = FirebaseError(ex.message.toString())
            }
        } else {
            viewState = NoUsers
        }
    }
}