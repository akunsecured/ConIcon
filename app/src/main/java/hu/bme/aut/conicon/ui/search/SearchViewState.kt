package hu.bme.aut.conicon.ui.search

import hu.bme.aut.conicon.network.model.AppUser

sealed class SearchViewState

object Initialize : SearchViewState()

object Loading : SearchViewState()

data class UsersReady(val userElements: MutableList<AppUser>) : SearchViewState()

data class FirebaseError(val message: String) : SearchViewState()

object NoUsers : SearchViewState()