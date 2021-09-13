package hu.bme.aut.conicon.ui.main.postupload

sealed class PostUploadViewState

object Initialize : PostUploadViewState()

object Loading : PostUploadViewState()

object UploadReady : PostUploadViewState()

data class FirebaseError(val message: String) : PostUploadViewState()