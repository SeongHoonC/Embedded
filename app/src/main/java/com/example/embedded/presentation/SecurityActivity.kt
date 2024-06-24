package com.example.embedded.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

    private val vm: SecurityViewModel by viewModels()

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
            vm.securityState.collect {
                updateUiState(it)
            }
        }
        initBtn()
    }

    private fun updateUiState(state: SecurityState) {
        when (state) {
            is SecurityState.SecurityOn -> {
                binding.tvState.text = "보안 작동중"
                binding.btnSecurity.visibility = View.GONE
                binding.btnSiren.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.btnWrongDetection.visibility = View.GONE
                binding.ivTheif.visibility = View.GONE
            }

            is SecurityState.Thief -> {
                binding.tvState.text = "도둑이야!!!"
                binding.btnSecurity.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.btnSiren.text = if (state.isSirenOn) "사이렌 끄기" else "사이렌 켜기"
                binding.btnSiren.visibility = View.VISIBLE
                binding.btnWrongDetection.visibility = View.VISIBLE
                binding.ivTheif.setImageBitmap(state.bitmap)
                Log.d("asdf", state.bitmap.toString())
                binding.ivTheif.visibility = View.VISIBLE
            }

            is SecurityState.SecurityOff -> {
                binding.tvState.text = "보안 꺼짐"
                binding.btnSecurity.text = "보안 켜기"
                binding.btnSiren.visibility = View.GONE
                binding.btnSecurity.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.btnWrongDetection.visibility = View.GONE
                binding.ivTheif.visibility = View.GONE
            }

            is SecurityState.ERROR -> {
                Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initBtn() {
        binding.btnSecurity.setOnClickListener {
            vm.securityOn()
        }
        binding.btnSiren.setOnClickListener {
            vm.changeSiren()
        }
        binding.btnWrongDetection.setOnClickListener {
            vm.wrongDetection()
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
