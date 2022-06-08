package hu.bme.aut.conicon.ui.chat

import hu.bme.aut.conicon.network.model.AppUser

sealed class ChatViewState

object Initialize : ChatViewState()

object Loading : ChatViewState()

data class UserDataReady(val user: AppUser) : ChatViewState()

data class FirebaseError(val message: String) : ChatViewState()

object UserNotFound : ChatViewState()