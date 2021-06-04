package com.capstone.rustdetector.source

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.capstone.rustdetector.source.remote.RemoteDataSource
import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import com.capstone.rustdetector.utils.Event
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.util.*


class RustDetectorRepository(private val remoteDataSource: RemoteDataSource) : RustDetectorDataSource{
    // status
    val isLoading = MutableLiveData<Boolean>()

    override fun getSegmentationResult(bitmap: Bitmap):
            LiveData<Event<CorrosionSegmentationResponse?>> {
        isLoading.value = true
        val mutableResponse = MutableLiveData<Event<CorrosionSegmentationResponse?>>()

        // preparation to build RequestBody
        val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val fileName : String = UUID.randomUUID().toString() + ".jpg"

        // compress bitmap
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos)
        builder.addFormDataPart(name = "imagefile", filename = fileName, RequestBody.create(
            MultipartBody.FORM, bos.toByteArray()
        ))

        // build RequestBody
        val requestBody : RequestBody = builder.build()

        // get result from remote source
        remoteDataSource.getCorrosionSegmentationResult(requestBody = requestBody, object :
            RemoteDataSource.CorrosionSegmentationCallback {
            override fun onItemsReceived(corrosionSegmentationResponse: CorrosionSegmentationResponse?) {
                isLoading.value = false

                val event = Event(corrosionSegmentationResponse)
                mutableResponse.postValue(event)
            }

            override fun onFailure() {
                isLoading.value = false
            }
        })

        return mutableResponse
    }


}