package hu.bme.aut.conicon.network.model

data class MediaElement (
        val id: String,
        val ownerID: String,
        val mediaLink: String,
        var likes: MutableList<String>,
        val comments: MutableList<CommentElement>,
        val details: String?
)