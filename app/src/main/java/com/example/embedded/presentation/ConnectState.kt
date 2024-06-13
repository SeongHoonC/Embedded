
package com.example.embedded.presentation

sealed interface ConnectState {
    data object Connected : ConnectState
    data object Disconnected : ConnectState
    data object Loading : ConnectState
}
