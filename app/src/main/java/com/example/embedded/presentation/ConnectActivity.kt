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
import com.example.embedded.presentation.BluetoothManager.bluetoothAdapter
import com.example.embedded.presentation.BluetoothManager.bluetoothSocket
import com.example.embedded.presentation.BluetoothManager.connectState
import com.example.embedded.presentation.BluetoothManager.initStreams
import java.io.IOException
import java.util.UUID

/*
  블루투스 연결하기를 화면을 그리고 실행하는 Activity 입니다.
 */

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
        noSystemBar()
    }

    /*
        블루투스를 사용하기 위한 BluetoothManager를 초기화합니다.
     */

    private fun initBlueToothMananger() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    /*
      블루투스 연결을 시도합니다.
      실패하면 Toast 메시지를 띄웁니다.
    */

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
            initStreams()
            connectState.value = ConnectState.Connected
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
            startActivity(SecurityActivity.getIntent(this))
            finish()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show()
        }
    }

    /*
        블루투스 연결 버튼을 눌렀을 때, 블루투스 권.한이 있는지 확인하고
        연결이 되었을 때, 보안 화면으로 이동합니다
    */

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

    /*
        시스템 바를 없애기 위한 함수입니다.
     */
    private fun noSystemBar() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /*
       블루투스 연결을 위한 UUID와 디바이스 주소입니다.
     */
    companion object {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val DEVICE_ADDRESS = "D8:3A:DD:00:E7:BD"
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }
}
