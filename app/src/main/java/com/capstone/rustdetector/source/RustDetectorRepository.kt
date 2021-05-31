package com.capstone.rustdetector.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class RustDetectorRepository {
    fun saveUserToFirebase(email : String, password : String) : LiveData<Array<String?>> {
        val mutableList = MutableLiveData<Array<String?>>()
        var status : String? = null
        var message : String? = null

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    status = "successful"
                    message = "Successfully created user with UUID ${it.result?.user?.uid}"
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

        mutableList.value = arrayOf(status, message)
        return mutableList
    }
}