package hu.bme.aut.conicon.network.model

data class MessageElement (
        val id: String,
        val sentBy: String,
        val sentTime: Long,
        val message: String,
        val isItMedia: Boolean,
        val mediaLink: String?
)