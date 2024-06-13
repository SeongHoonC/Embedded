package com.example.embedded.presentation

sealed interface ConnectState {
    data object Connected : ConnectState
    data object Disconnected : ConnectState
    data object Loading : ConnectState
}

sealed interface SecurityState {
    data object SecurityOn : SecurityState
    data class Thief(val isSirenOn: Boolean) : SecurityState
    data object SecurityOff : SecurityState
}
