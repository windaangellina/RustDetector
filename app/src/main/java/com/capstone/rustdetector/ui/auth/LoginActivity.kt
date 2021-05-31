package com.capstone.rustdetector.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityLoginBinding
import com.capstone.rustdetector.databinding.ActivityRegisterBinding
import com.capstone.rustdetector.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        setComponentEvents()
    }

    private fun setComponentEvents(){
        binding.layoutLogin.textViewGoToRegister.setOnClickListener { goToRegister() }
        binding.layoutLogin.buttonLogin.setOnClickListener { login() }
    }

    private fun goToRegister(){
        val registerIntent : Intent = Intent(applicationContext, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun goToHome(){
        val homeIntent : Intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

    private fun login(){
       goToHome()
    }
}