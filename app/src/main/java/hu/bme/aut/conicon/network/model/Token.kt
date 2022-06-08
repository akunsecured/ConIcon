package hu.bme.aut.conicon.network.model

data class Token (
    val userID: String? = null,
    val tokens: HashMap<String, *> = hashMapOf<String, Boolean>()
)