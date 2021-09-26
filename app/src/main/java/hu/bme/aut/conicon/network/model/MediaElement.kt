package hu.bme.aut.conicon.network.model

data class MediaElement (
        val id: String = "",
        val date: Long = -1,
        val ownerID: String = "",
        val mediaLink: String = "",
        var likes: MutableList<String> = mutableListOf(),
        val comments: MutableList<String> = mutableListOf(),
        val details: String? = null
)