package hu.bme.aut.conicon.ui.main.home

import hu.bme.aut.conicon.network.model.MediaElement

sealed class HomeViewState

object Initialize : HomeViewState()

object Loading : HomeViewState()

data class PostsReady(val posts: MutableList<MediaElement>) : HomeViewState()

data class FirebaseError(val message: String) : HomeViewState()