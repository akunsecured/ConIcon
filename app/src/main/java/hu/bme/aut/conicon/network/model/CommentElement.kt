package hu.bme.aut.conicon.network.model

data class CommentElement (
        val id: String,
        val sentBy: String,
        val message: String,
        val sentTime: Long
)