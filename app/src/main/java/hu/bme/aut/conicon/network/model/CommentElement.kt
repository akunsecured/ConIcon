package hu.bme.aut.conicon.network.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class CommentElement (
        val id: String = "",
        val sentBy: String = "",
        val message: String = "",
        val sentTime: Long = -1,
        @ServerTimestamp var time: Date? = null
)