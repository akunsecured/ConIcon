package hu.bme.aut.conicon.ui.post

import hu.bme.aut.conicon.network.model.AppUser

sealed class PostViewState

object Initialize : PostViewState()

object Loading : PostViewState()

data class UserDataReady(val user: AppUser) : PostViewState()

data class FirebaseError(val message: String) : PostViewState()