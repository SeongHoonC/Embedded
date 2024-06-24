package com.example.embedded.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream
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

    fun receiveImage(): Result<Bitmap> {
        return runCatching {
            val sizeBuffer = ByteArray(1024)
            val sizeBytes =
                inputStream?.read(sizeBuffer) ?: throw IllegalStateException("Failed to read size")
            val imageSize = String(sizeBuffer, 0, sizeBytes).toInt()
            outputStream?.write("READY".toByteArray())  // 서버에 준비 상태 알림

            val buffer = ByteArray(1024)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var totalBytesRead = 0
            var bytesRead: Int

            while (totalBytesRead < imageSize) {
                bytesRead = inputStream?.read(buffer) ?: break
                if (bytesRead == -1) {
                    throw IllegalStateException("Socket closed or read error")
                }
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
            }

            val imageBytes = byteArrayOutputStream.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    fun disconnect() {
        sendData("q")
        outputStream?.close()
        inputStream?.close()
        bluetoothSocket?.close()
        outputStream = null
        inputStream = null
        bluetoothSocket = null
        connectState.value = ConnectState.Disconnected
    }
}
