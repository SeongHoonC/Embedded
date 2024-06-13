package com.example.embedded.presentation

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.embedded.R
import com.example.embedded.databinding.ActivityConnectBinding
import com.example.embedded.presentation.TreasureSecurity.bluetoothAdapter
import com.example.embedded.presentation.TreasureSecurity.bluetoothSocket
import com.example.embedded.presentation.TreasureSecurity.connectState
import com.example.embedded.presentation.TreasureSecurity.outputStream
import java.io.IOException
import java.util.UUID

class ConnectActivity : AppCompatActivity() {

    private val binding: ActivityConnectBinding by lazy {
        ActivityConnectBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initBlueToothMananger()
        initBtn()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initBlueToothMananger() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectBluetooth() {
        val device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(DEVICE_ADDRESS)
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_PERMISSIONS,
                )
                return
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket!!.connect()
            outputStream = bluetoothSocket!!.outputStream
            connectState.value = ConnectState.Connected
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
            startActivity(LEDActivity.getIntent(this))
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun initBtn() {
        binding.btnConnect.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_PERMISSIONS
                )
            } else {
                connectState.value = ConnectState.Loading
                connectBluetooth()
            }
        }
    }

    companion object {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val DEVICE_ADDRESS = "D8:3A:DD:00:E7:BD"
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }
}
