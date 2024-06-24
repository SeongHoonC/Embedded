package com.example.embedded.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.embedded.presentation.BluetoothManager.connectState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SecurityViewModel : ViewModel() {

    private val _securityState: MutableStateFlow<SecurityState> =
        MutableStateFlow(SecurityState.SecurityOff)
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()
    private var isSirenOn = false

    fun securityOn() {
        viewModelScope.launch {
            BluetoothManager.sendData("SECURITY_ON")
                .onSuccess {
                    _securityState.value = SecurityState.SecurityOn
                }.handleConnectFailure()
                .onFailure {
                    return@launch
                }

            launch(Dispatchers.IO) {
                BluetoothManager.receiveImage()
                    .onSuccess {
                        _securityState.value = SecurityState.Thief(it, false)
                    }.handleConnectFailure()
            }
        }
    }


    fun changeSiren() {
        val thief = securityState.value as? SecurityState.Thief ?: return

        viewModelScope.launch {
            if (!thief.isSirenOn) {
                _securityState.value = SecurityState.Thief(thief.bitmap, true)
                isSirenOn = true
                while (connectState.value == ConnectState.Connected && isSirenOn && securityState.value is SecurityState.Thief) {
                    runCatching {
                        BluetoothManager.sendData("SIREN_ON")
                        delay(300)
                    }.handleConnectFailure()
                }
                return@launch
            }
            BluetoothManager.sendData("SIREN_OFF")
                .onSuccess {
                    _securityState.value = SecurityState.Thief(thief.bitmap, false)
                    isSirenOn = false
                    while (connectState.value == ConnectState.Connected && !isSirenOn && securityState.value is SecurityState.Thief) {
                        runCatching {
                            BluetoothManager.sendData("SIREN_OFF")
                            delay(300)
                        }.handleConnectFailure()
                    }
                }.handleConnectFailure()
        }
    }

    fun wrongDetection() {
        viewModelScope.launch {
            BluetoothManager.sendData("WRONG_DETECTION")
                .onSuccess {
                    _securityState.value = SecurityState.SecurityOff
                }.handleConnectFailure()

        }
    }

    private fun <T> Result<T>.handleConnectFailure() = onFailure {
        BluetoothManager.disconnect()
        Log.d("embedded", it.toString())
        _securityState.value = SecurityState.ERROR
    }
}
