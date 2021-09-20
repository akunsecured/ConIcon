package hu.bme.aut.conicon.ui.conversations

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationsViewModel @Inject constructor(

) : RainbowCakeViewModel<ConversationsViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }
}