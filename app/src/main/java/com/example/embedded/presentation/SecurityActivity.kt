package com.example.embedded.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.embedded.R
import com.example.embedded.databinding.ActivityLedactivityBinding
import kotlinx.coroutines.launch
import java.io.IOException

class SecurityActivity : AppCompatActivity() {

    private val binding: ActivityLedactivityBinding by lazy {
        ActivityLedactivityBinding.inflate(layoutInflater)
    }

    private val securityViewModel: SecurityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        initWindowPadding()
        initBtn()
        initView()
    }

    private fun initWindowPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initView() {
        lifecycleScope.launch {
            securityViewModel.securityState.collect {
                updateUiState(it)
            }
        }
        initBtn()
    }

    private fun updateUiState(state: SecurityState) {
        when (state) {
            is SecurityState.SecurityOn -> {
                binding.tvState.text = "보안 켜짐"
            }

            is SecurityState.Thief -> {
                binding.tvState.text =
                    if (state.isSirenOn) "도둑이 들어왔습니다. 사이렌 켜짐" else "도둑이 들어왔습니다. 사이렌 꺼짐"
            }

            is SecurityState.SecurityOff -> {
                binding.tvState.text = "보안 꺼짐"
            }
        }
    }

    private fun initBtn() {
        binding.btnOnOff.setOnClickListener {
            securityViewModel.changeSiren()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothManager.disconnect()
        Toast.makeText(this, "연결을 중지합니다", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SecurityActivity::class.java)
        }
    }

}
