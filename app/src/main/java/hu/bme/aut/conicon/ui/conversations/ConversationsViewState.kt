package hu.bme.aut.conicon.ui.conversations

import hu.bme.aut.conicon.network.model.ConversationElement

sealed class ConversationsViewState

object Initialize : ConversationsViewState()

object Loading : ConversationsViewState()

data class FirebaseError(val message: String) : ConversationsViewState()

data class ConversationsReady(val conversationElements: MutableList<ConversationElement>) : ConversationsViewState()