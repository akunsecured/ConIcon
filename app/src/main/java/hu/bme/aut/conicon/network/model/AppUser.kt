package hu.bme.aut.conicon.network.model

data class AppUser (
        val id: String = "",
        val username: String = "",
        val email: String = "",
        var photoUrl: String? = null,
        val followers: MutableList<String> = mutableListOf(),
        val following: MutableList<String> = mutableListOf(),
        val posts: MutableList<String> = mutableListOf()
)