package com.capstone.rustdetector.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        setComponentEvents()
    }

    private fun setComponentEvents(){
        binding.layoutRegister.textViewGoToLogin.setOnClickListener { goToLogin() }
    }

    private fun goToLogin(){
        val loginIntent : Intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}