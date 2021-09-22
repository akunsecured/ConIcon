package hu.bme.aut.conicon.ui.chat

sealed class ChatViewState

object Initialize : ChatViewState()

object Loading : ChatViewState()