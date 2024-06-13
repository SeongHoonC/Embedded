package com.example.embedded.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.InputStream
import java.io.OutputStream

object BluetoothManager {
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
    var connectState: MutableStateFlow<ConnectState> = MutableStateFlow(ConnectState.Disconnected)
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null


    fun initStreams() {
        bluetoothSocket?.let {
            outputStream = it.outputStream
            inputStream = it.inputStream
        }
    }

    fun sendData(data: String): Result<Unit> {
        return runCatching {
            outputStream!!.write(data.toByteArray())
        }
    }

    fun receiveData(): Result<String> {
        return runCatching {
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer) ?: throw IllegalStateException("InputStream is null")
            String(buffer, 0, bytes)
        }
    }

    fun disconnect() {
        outputStream?.close()
        inputStream?.close()
        bluetoothSocket?.close()
        outputStream = null
        inputStream = null
        bluetoothSocket = null
        connectState.value = ConnectState.Disconnected
    }
}
