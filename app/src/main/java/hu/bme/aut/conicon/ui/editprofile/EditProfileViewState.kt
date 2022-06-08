package hu.bme.aut.conicon.ui.editprofile

sealed class EditProfileViewState

object Initialize : EditProfileViewState()

object Loading : EditProfileViewState()

object SuccessfullyUpdated : EditProfileViewState()

data class FirebaseError(val message: String) : EditProfileViewState()

object UsernameTakenError : EditProfileViewState()

object UploadReady : EditProfileViewState()

object ProfileDeleted : EditProfileViewState()