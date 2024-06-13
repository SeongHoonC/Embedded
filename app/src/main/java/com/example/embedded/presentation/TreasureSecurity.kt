package com.example.embedded.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecurityViewModel : ViewModel() {

    private val _securityState: MutableStateFlow<SecurityState> =
        MutableStateFlow(SecurityState.SecurityOff)
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()


    fun securityOn() {
        viewModelScope.launch {
            while (BluetoothManager.connectState.value == ConnectState.Connected) {
                val result = BluetoothManager.receiveData()
                result.onSuccess { data ->
                    // 데이터 수신 성공
                    if (data == "THIEF") {
                        _securityState.value = SecurityState.Thief(false)
                    }
                }.onFailure { exception ->
                    // 데이터 수신 중 오류 발생
                    println("Error receiving data: ${exception.message}")
                    BluetoothManager.disconnect()
                }
            }
        }
    }

    fun changeSiren() {
        val thief = securityState.value as? SecurityState.Thief ?: return

        if (!thief.isSirenOn) {
            BluetoothManager.sendData("SIREN_ON").onSuccess {
                _securityState.value = SecurityState.Thief(true)
            }.onFailure {
                Log.d("asdf", it.toString())
            }
            return
        }
        BluetoothManager.sendData("SIREN_OFF")
            .onSuccess {
                _securityState.value = SecurityState.Thief(false)
            }.onFailure {
                Log.d("asdf", it.toString())
            }
    }
}
