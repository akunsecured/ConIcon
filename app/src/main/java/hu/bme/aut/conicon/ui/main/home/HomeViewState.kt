package hu.bme.aut.conicon.ui.main.home

sealed class HomeViewState

object Initialize : HomeViewState()

object Loading : HomeViewState()