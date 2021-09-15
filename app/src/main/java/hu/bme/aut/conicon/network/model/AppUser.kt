package hu.bme.aut.conicon.network.model

data class AppUser (
        val id: String,
        val username: String,
        val email: String,
        var photoUrl: String?,
        val followers: MutableList<String>,
        val following: MutableList<String>,
        val posts: MutableList<String>
)