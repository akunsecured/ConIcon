package hu.bme.aut.conicon.ui.main.profile

sealed class ProfileViewState

object Initialize : ProfileViewState()

object Loading : ProfileViewState()