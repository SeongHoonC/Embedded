package com.example.embedded.presentation

/*
  Bluetooth 연결의 각 상태를 나타내는 Sealed Interface
 */

sealed interface ConnectState {
    data object Connected : ConnectState
    data object Disconnected : ConnectState
    data object Loading : ConnectState
}
