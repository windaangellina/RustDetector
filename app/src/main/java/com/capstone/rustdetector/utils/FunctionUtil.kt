package com.capstone.rustdetector.utils

import android.content.Context
import android.widget.Toast

object FunctionUtil {
    fun makeToast(context: Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}