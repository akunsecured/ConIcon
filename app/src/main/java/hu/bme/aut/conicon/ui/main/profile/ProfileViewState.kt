package hu.bme.aut.conicon.ui.main.profile

import hu.bme.aut.conicon.network.model.AppUser

sealed class ProfileViewState

object Initialize : ProfileViewState()

object Loading : ProfileViewState()

data class DatabaseError(val message: String) : ProfileViewState()

data class UserDataReady(val user: AppUser) : ProfileViewState()

object NoUserWithThisUID : ProfileViewState()

data class ConversationReady(val conversationID: String, val userID: String) : ProfileViewState()