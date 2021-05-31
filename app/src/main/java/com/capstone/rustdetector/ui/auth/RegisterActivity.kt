package com.capstone.rustdetector.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.rustdetector.utils.FunctionUtil
import com.capstone.rustdetector.databinding.ActivityRegisterBinding
import com.capstone.rustdetector.viewmodel.RustDetectorViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    // view model
    private val rustDetectorViewModel : RustDetectorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        setComponentEvents()
    }

    private fun setComponentEvents(){
        binding.layoutRegister.textViewGoToLogin.setOnClickListener { goToLogin() }
        binding.layoutRegister.buttonSubmitRegister.setOnClickListener { register() }
    }

    private fun goToLogin(){
        val loginIntent : Intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun register(){
        val email = binding.layoutRegister.editTextEmail.text.toString()
        val password = binding.layoutRegister.editTextPassword.text.toString()
        val confirmPassword = binding.layoutRegister.editTextConfirmPassword.text.toString()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            FunctionUtil.makeToast(applicationContext, "Please fill all fields")
        }
        else{
            if (password != confirmPassword){
                FunctionUtil.makeToast(applicationContext,
                    "Password and confirm password don't match")
            }
            else{
                saveToFirebase(email, password)
//                rustDetectorViewModel.saveUserToFirebase(email, password).observe(this, {
//                    val status = it[0]
//                    val message = it[1]
//
////                    when(status){
////                        "successful" -> {
////
////                        }
////                        "failed" -> {
////
////                        }
////                        "canceled" -> {
////
////                        }
////                    }
//
//                    if (message != null) {
//                        FunctionUtil.makeToast(applicationContext, message)
//                    }
//                    else{
//                        FunctionUtil.makeToast(applicationContext, "empty message")
//                    }
//                })
            }
        }
    }

    private fun saveToFirebase(email : String, password : String){
        var status : String? = null
        var message : String? = null

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    status = "successful"
                    message = "Successfully created user with UUID ${it.result?.user?.uid}"
                }
                else{
                    message = "Failed on complete to create user due to ${it.exception?.message}"
                }
            }
            .addOnFailureListener{
                status = "failed"
                message = "Failed to create user due to ${it.message}"
            }
            .addOnCanceledListener {
                status = "canceled"
                message = "Creating new user has been cancelled"
            }

        if (message != null) {
            FunctionUtil.makeToast(applicationContext, message!!)
        }
        else{
            FunctionUtil.makeToast(applicationContext, "empty message")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}