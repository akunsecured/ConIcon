package hu.bme.aut.conicon.ui.comment

import hu.bme.aut.conicon.network.model.CommentElement

sealed class CommentViewState

object Initialize : CommentViewState()

object Loading : CommentViewState()

data class CommentsReady(val comments: MutableList<CommentElement>) : CommentViewState()

data class FirebaseError(val message: String) : CommentViewState()