package hu.bme.aut.conicon.network.model

data class ConversationElement (
        val id: String = "",
        val participantIDs: MutableList<String> = mutableListOf(),
        var lastMessage: MessageElement? = null,
        val messages: MutableList<MessageElement> = mutableListOf()
)
