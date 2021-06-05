package com.capstone.rustdetector.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.IOException
import java.util.*


object FunctionUtil {
    private const val TAG = "FunctionUtil"

    private fun getExtensionFromUri(uri: Uri, context: Context): String? {
        val cR: ContentResolver = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    fun getFilenameFromUri(uri: Uri?, context: Context) : String? {
        // returns randomized unique filename with extension
        return UUID.randomUUID().toString() + "." + uri?.let { getExtensionFromUri(it, context) }
    }

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

    fun getBitmapFromUri(uri : Uri?, context: Context) : Bitmap?{
        var bitmap: Bitmap? = null
        try {
            if(Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver, uri
                )
            } else {
                val source =
                    uri?.let {
                        ImageDecoder.createSource(context.contentResolver,
                            it
                        )
                    }
                bitmap = source?.let { ImageDecoder.decodeBitmap(it) }
                bitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
            }
        }catch (e : IOException){
            Log.e(TAG, "Upload and convert image failed : ${e.message}")
        }

        return bitmap
    }
}