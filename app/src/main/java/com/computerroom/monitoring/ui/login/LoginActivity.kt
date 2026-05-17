package com.computerroom.monitoring.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.computerroom.monitoring.databinding.ActivityLoginBinding
import com.computerroom.monitoring.ui.home.MainActivity
import com.computerroom.monitoring.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (viewModel.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isRegisterMode) {
                viewModel.register(email, password)
            } else {
                viewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateMode()
        }
    }

    private fun updateMode() {
        if (isRegisterMode) {
            binding.tvTitle.text = "Đăng ký"
            binding.btnLogin.text = "Đăng ký"
            binding.tvRegister.text = "Đăng nhập"
        } else {
            binding.tvTitle.text = "Smart Farm Monitor"
            binding.btnLogin.text = "Đăng nhập"
            binding.tvRegister.text = "Đăng ký"
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }
}
