package hu.bme.aut.conicon.network.model

data class ConversationElement (
        val id: String = "",
        val participantIDs: HashMap<String, *> = hashMapOf<String, Boolean>(),
        var lastMessage: MessageElement? = null
)
