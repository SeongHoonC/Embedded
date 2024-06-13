package com.example.embedded.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.OutputStream
import java.util.UUID

object TreasureSecurity {
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
    var outputStream: OutputStream? = null
    var connectState: MutableStateFlow<ConnectState> = MutableStateFlow(ConnectState.Disconnected)
}
