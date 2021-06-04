package com.capstone.rustdetector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.capstone.rustdetector.ui.home.MainActivity
import java.io.IOException
import kotlin.random.Random

object FunctionUtil {
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private const val STRING_LENGTH = 36
    private const val ALPHANUMERIC_REGEX = "[a-zA-Z0-9]+";

    fun makeToast(context: Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showImageByUrlUsingGlide(context: Context, imageUrl : String, imageView: ImageView){
        Glide.with(context)
            .load(imageUrl)
            .into(imageView)
    }

    fun showImageByBitmapUsingGlide(context: Context, bitmap : Bitmap, imageView: ImageView){
        Glide.with(context)
            .asBitmap()
            .load(bitmap)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })
    }


}