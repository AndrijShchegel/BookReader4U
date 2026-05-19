package com.andriishchehel.bookreader4u.ui.authorization

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.ActivityLoginBinding
import com.andriishchehel.bookreader4u.ui.MainActivity
import com.andriishchehel.bookreader4u.ui.admin.AdminActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.fetchCurrentUserId().isSuccess) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setListeners() {
        binding.textViewNavToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextLoginEmail.text.toString()
            val password = binding.editTextLoginPassword.text.toString()

            lifecycleScope.launch {
                viewModel.login(email, password).fold(
                    onSuccess = {
                        processSingIn()
                    },
                    onFailure = {
                        binding.inputLayoutLoginEmail.error =
                            getString(R.string.error_wrong_email_or_password)
                        binding.inputLayoutLoginPassword.error =
                            getString(R.string.error_wrong_email_or_password)
                    }
                )
            }
        }

        binding.editTextLoginEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.editTextLoginEmail.text.toString()
                when (viewModel.validateEmail(email)) {
                    "email has wrong pattern" -> binding.inputLayoutLoginEmail.error =
                        getString(R.string.error_wrong_email_pattern)
                }

            }
        }

        binding.editTextLoginEmail.doAfterTextChanged {
            binding.inputLayoutLoginEmail.error = null
        }

        binding.editTextLoginPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.editTextLoginPassword.text.toString()
                when (viewModel.validatePassword(password)) {
                    "password < 6" -> binding.inputLayoutLoginPassword.error =
                        getString(R.string.error_password_length)
                }
            }
        }

        binding.editTextLoginPassword.doAfterTextChanged {
            binding.inputLayoutLoginPassword.error = null
        }
    }

    private fun processSingIn() {
        lifecycleScope.launch {
            viewModel.checkUserStatus().fold(
                onSuccess = { role ->
                    if (role == "admin") {
                        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                },
                onFailure = {
                    Toast.makeText(this@LoginActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                    return@launch
                }
            )
        }
    }
}