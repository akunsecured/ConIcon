package hu.bme.aut.conicon.network.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class MessageElement (
        var id: String = "",
        var sentBy: String = "",
        var message: String = "",
        var sentFromClient: Long = -1,
        var isItMedia: Boolean = false,
        var mediaLink: String? = null,
        @ServerTimestamp var time: Date? = null
)