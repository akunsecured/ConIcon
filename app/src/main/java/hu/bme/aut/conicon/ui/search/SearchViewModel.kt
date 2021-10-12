package hu.bme.aut.conicon.ui.search

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.conicon.network.model.AppUser
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(

) : RainbowCakeViewModel<SearchViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun searchUsers(search: String) = viewModelScope.launch {
        viewState = Loading

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val query = userCollection.whereGreaterThanOrEqualTo("username", search)
                .whereLessThanOrEqualTo("username", search + '\uf8ff')

        query.get().addOnSuccessListener { querySnapshot ->
            val users = mutableListOf<AppUser>()
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    users.add(
                            document.toObject(AppUser::class.java)!!
                    )
                }
            }

            viewState =
                    if (users.isEmpty()) NoUsers
                    else UsersReady(users)
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}