package hu.bme.aut.conicon.ui.setusername

sealed class SetUsernameViewState

object Initialize : SetUsernameViewState()

object Loading : SetUsernameViewState()

object UsernameTakenError : SetUsernameViewState()

data class DatabaseError(val message: String) : SetUsernameViewState()

object SuccessfullyRegistered : SetUsernameViewState()