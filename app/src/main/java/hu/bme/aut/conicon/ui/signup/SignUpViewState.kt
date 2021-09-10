package hu.bme.aut.conicon.ui.signup

sealed class SignUpViewState

object Initialize : SignUpViewState()

object Loading : SignUpViewState()

data class UsernameError(val message: String) : SignUpViewState()

data class SignUpError(val message: String) : SignUpViewState()

data class DatabaseError(val message: String) : SignUpViewState()

object SignUpReady : SignUpViewState()