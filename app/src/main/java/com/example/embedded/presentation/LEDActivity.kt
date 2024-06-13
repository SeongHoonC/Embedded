package com.example.embedded.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.embedded.R
import com.example.embedded.databinding.ActivityLedactivityBinding
import java.io.IOException

class LEDActivity : AppCompatActivity() {
    private var isLedOn = false

    private val binding: ActivityLedactivityBinding by lazy {
        ActivityLedactivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTvLed()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initBtn()
    }

    private fun initBtn() {
        binding.btnOnOff.setOnClickListener {
            isLedOn = !isLedOn
            setTvLed()
            sendData(if (isLedOn) "ON" else "OFF")
        }
    }

    private fun setTvLed() {
        binding.tvLED.text = if (isLedOn) "LED On" else "LED Off"
    }

    private fun sendData(data: String) {
        if (TreasureSecurity.outputStream != null) {
            try {
                TreasureSecurity.outputStream!!.write(data.toByteArray())
            } catch (e: IOException) {
                Log.e("asdf", "Error sending data", e)
                Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sendData("q")
        Toast.makeText(this, "연결을 중지합니다", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, LEDActivity::class.java)
        }
    }

}
