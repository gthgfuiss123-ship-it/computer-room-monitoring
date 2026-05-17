package com.computerroom.monitoring.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.computerroom.monitoring.R
import com.computerroom.monitoring.databinding.ActivityLoginBinding
import com.computerroom.monitoring.ui.home.MainActivity
import com.computerroom.monitoring.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar

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
            binding.tilEmail.error = null
            binding.tilPassword.error = null

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
            binding.tilEmail.error = null
            binding.tilPassword.error = null
            updateMode()
        }
    }

    private fun updateMode() {
        if (isRegisterMode) {
            binding.tvTitle.text = "Dang ky"
            binding.btnLogin.text = "Dang ky"
            binding.tvRegister.text = "Dang nhap"
        } else {
            binding.tvTitle.text = "Smart Farm Monitor"
            binding.btnLogin.text = "Dang nhap"
            binding.tvRegister.text = "Dang ky"
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.emailError.observe(this) { error ->
            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.tilPassword.error = error
        }

        viewModel.errorMessage.observe(this) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.warning_red))
                .setTextColor(getColor(R.color.white))
                .show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }
}
