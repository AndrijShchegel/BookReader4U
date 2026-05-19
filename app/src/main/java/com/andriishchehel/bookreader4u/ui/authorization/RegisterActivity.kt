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
import com.andriishchehel.bookreader4u.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()

    }

    private fun setListeners() {
        binding.textViewNavToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.buttonRegister.setOnClickListener { onRegisterClicked() }

        binding.editTextRegisterEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.editTextRegisterEmail.text.toString()
                when (viewModel.validateEmail(email)) {
                    "email has wrong pattern" -> binding.inputLayoutRegisterEmail.error =
                        getString(R.string.error_wrong_email_pattern)
                }
            }
        }

        binding.editTextRegisterEmail.doAfterTextChanged {
            binding.inputLayoutRegisterEmail.error = null
        }

        binding.editTextRegisterUsername.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val username = binding.editTextRegisterUsername.text.toString()
                when (viewModel.validateUsername(username)) {
                    "username < 3" -> binding.inputLayoutRegisterUsername.error =
                        getString(R.string.error_username_length)
                }
            }
        }

        binding.editTextRegisterUsername.doAfterTextChanged {
            binding.inputLayoutRegisterUsername.error = null
        }

        binding.editTextRegisterPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.editTextRegisterPassword.text.toString()
                when (viewModel.validatePassword(password)) {
                    "password < 6" -> binding.inputLayoutRegisterPassword.error =
                        getString(R.string.error_password_length)
                }
            }
        }

        binding.editTextRegisterPassword.doAfterTextChanged {
            binding.inputLayoutRegisterPassword.error = null
        }

        binding.editTextRegisterConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.editTextRegisterConfirmPassword.text.toString()
                when (viewModel.validatePassword(password)) {
                    "password < 6" -> binding.inputLayoutRegisterConfirmPassword.error =
                        getString(R.string.error_password_length)
                }
            }
        }

        binding.editTextRegisterConfirmPassword.doAfterTextChanged {
            binding.inputLayoutRegisterConfirmPassword.error = null
        }
    }

    private fun onRegisterClicked() {
        val username = binding.editTextRegisterUsername.text.toString()
        val email = binding.editTextRegisterEmail.text.toString()
        val password = binding.editTextRegisterPassword.text.toString()
        val confirmPassword = binding.editTextRegisterConfirmPassword.text.toString()
        when {
            username.isEmpty() -> {
                binding.inputLayoutRegisterUsername.error = getString(R.string.error_empty_field)
                return
            }

            email.isEmpty() -> {
                binding.inputLayoutRegisterEmail.error = getString(R.string.error_empty_field)
                return
            }

            password.isEmpty() -> {
                binding.inputLayoutRegisterPassword.error = getString(R.string.error_empty_field)
                return
            }

            confirmPassword.isEmpty() -> {
                binding.inputLayoutRegisterConfirmPassword.error =
                    getString(R.string.error_empty_field)
                return
            }
        }
        when (viewModel.isValidInput(username, email, password, confirmPassword)) {
            "passwords not match" -> {
                binding.inputLayoutRegisterPassword.error =
                    getString(R.string.error_match_passwords)
                binding.inputLayoutRegisterConfirmPassword.error =
                    getString(R.string.error_match_passwords)
            }

            "check inputs" -> {
                Toast.makeText(this, getString(R.string.error_check_fields), Toast.LENGTH_LONG)
                    .show()
            }
        }
        lifecycleScope.launch {
            viewModel.register(username, email, password).fold(
                onSuccess = {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onFailure = {
                    Toast.makeText(this@RegisterActivity, it.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }
            )
        }
    }
}