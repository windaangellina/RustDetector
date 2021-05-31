package com.capstone.rustdetector.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.capstone.rustdetector.source.RustDetectorRepository
import com.google.firebase.auth.FirebaseAuth

class RustDetectorViewModel(private val rustDetectorRepository: RustDetectorRepository) : ViewModel() {
    fun saveUserToFirebase(email : String, password : String) : LiveData<Array<String?>>{
        return rustDetectorRepository.saveUserToFirebase(email, password)
    }
}