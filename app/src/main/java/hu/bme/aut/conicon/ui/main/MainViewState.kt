package hu.bme.aut.conicon.ui.main

sealed class MainViewState

object Initialize : MainViewState()

object Loading : MainViewState()