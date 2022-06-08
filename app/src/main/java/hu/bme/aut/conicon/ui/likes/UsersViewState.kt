package hu.bme.aut.conicon.ui.likes

import hu.bme.aut.conicon.network.model.AppUser

sealed class UsersViewState

object Initialize : UsersViewState()

object Loading : UsersViewState()

data class UsersReady(val userElements: MutableList<AppUser>) : UsersViewState()

data class FirebaseError(val message: String) : UsersViewState()

object NoUsers : UsersViewState()