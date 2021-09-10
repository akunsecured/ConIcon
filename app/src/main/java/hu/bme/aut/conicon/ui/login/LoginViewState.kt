package hu.bme.aut.conicon.ui.login

sealed class LoginViewState

object Initialize : LoginViewState()

object Loading : LoginViewState()

object SuccessfulLogin : LoginViewState()

data class LoginError(val message: String) : LoginViewState()

data class DatabaseError(val message: String) : LoginViewState()

object SetUsername : LoginViewState()