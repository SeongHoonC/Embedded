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
        initObserving()
    }

    // 현재 보안 상태를 관찰합니다.
    private fun initObserving() {
        lifecycleScope.launch {
            vm.securityState.collect {
                updateUiState(it)
            }
        }
    }

    private fun updateUiState(state: SecurityState) {
        when (state) {
            // 보안 작동 중 화면을 업데이트합니다.
            is SecurityState.SecurityOn -> {
                binding.tvState.text = "보안 작동중"
                binding.btnSecurity.visibility = View.GONE
                binding.btnSiren.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.btnWrongDetection.visibility = View.GONE
                binding.ivTheif.visibility = View.GONE
            }

            // 도둑이 감지 됐을 때 화면을 업데이트합니다.
            is SecurityState.Thief -> {
                binding.tvState.text = "도둑이야!!!"
                binding.btnSecurity.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.btnSiren.text = if (state.isSirenOn) "사이렌 끄기" else "사이렌 켜기"
                binding.btnSiren.visibility = View.VISIBLE
                binding.btnWrongDetection.visibility = View.VISIBLE
                binding.ivTheif.setImageBitmap(state.bitmap)
                binding.ivTheif.visibility = View.VISIBLE
            }

            // 보안 꺼짐 상태일 때 화면을 업데이트합니다.
            is SecurityState.SecurityOff -> {
                binding.tvState.text = "보안 꺼짐"
                binding.btnSecurity.text = "보안 켜기"
                binding.btnSiren.visibility = View.GONE
                binding.btnSecurity.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.btnWrongDetection.visibility = View.GONE
                binding.ivTheif.visibility = View.GONE
            }

            // 에러 발생 시 토스트 메시지를 띄우고 화면을 종료합니다.
            is SecurityState.ERROR -> {
                Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // 버튼을 클릭할 때 이벤트를 정의합니다.
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

    // 화면의 여백을 설정합니다.
    private fun initWindowPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 화면 종료 시, 블루투스 연결을 종료합니다.
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
