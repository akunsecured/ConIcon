package hu.bme.aut.conicon.network.model

data class Conversation (
        val id: String,
        val participantIDs: MutableList<String>,
        var lastMessage: MessageElement,
        val messages: MutableList<MessageElement>
)
