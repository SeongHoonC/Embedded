package com.example.embedded.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/*
    블루투스 상태 및 소캣 통신을 관리하는 싱글톤 객체입니다.
 */

object BluetoothManager {
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
    var connectState: MutableStateFlow<ConnectState> = MutableStateFlow(ConnectState.Disconnected)
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null


    // 소켓 통신을 위한 스트림을 초기화하는 함수입니다.
    fun initStreams() {
        bluetoothSocket?.let {
            outputStream = it.outputStream
            inputStream = it.inputStream
        }
    }

    // 데이터를 전송하는 함수입니다.
    fun sendData(data: String): Result<Unit> {
        return runCatching {
            outputStream!!.write(data.toByteArray())
        }
    }

    // 이미지를 수신하는 함수입니다.
    fun receiveImage(): Result<Bitmap> {
        return runCatching {
            val imageSize = receiveImageSize()
            val buffer = ByteArray(1024)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var totalBytesRead = 0
            var bytesRead: Int

            // 이미지 데이터를 읽어들입니다.
            while (totalBytesRead < imageSize) {
                bytesRead = inputStream?.read(buffer) ?: break
                if (bytesRead == -1) {
                    throw IllegalStateException("Socket closed or read error")
                }
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
            }

            // 이미지 데이터를 비트맵으로 변환합니다.
            val imageBytes = byteArrayOutputStream.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    // 이미지 사이즈를 미리 받아서 이미지 데이터를 읽어들입니다.
    private fun receiveImageSize(): Int {
        val sizeBuffer = ByteArray(1024)
        val sizeBytes =
            inputStream?.read(sizeBuffer) ?: throw IllegalStateException("Failed to read size")
        val imageSize = String(sizeBuffer, 0, sizeBytes).toInt()
        outputStream?.write("READY".toByteArray())  // 서버에 준비 상태 알림
        return imageSize
    }

    // 연결을 끊는 함수입니다.
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
