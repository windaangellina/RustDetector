package com.capstone.rustdetector.source.remote

import android.util.Log
import com.capstone.rustdetector.source.remote.api.RetrofitClient
import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import com.capstone.rustdetector.utils.EspressoIdlingResource
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteDataSource {
    companion object {
        private const val TAG = "RemoteDataSource"
    }

    fun getCorrosionSegmentationResult(requestBody: RequestBody, callback : CorrosionSegmentationCallback) {
        EspressoIdlingResource.increment()
        RetrofitClient.instanceCorrosionSegmentationApi.getCorrosionSegmentationResult(requestBody)
            .enqueue(
            object : Callback<CorrosionSegmentationResponse?>{
                override fun onResponse(
                    call: Call<CorrosionSegmentationResponse?>,
                    response: Response<CorrosionSegmentationResponse?>
                ) {
                    if (response.isSuccessful){
                        response.body().let { callback.onItemsReceived(it) }
                    }
                    else{
                        callback.onFailure()
                        Log.e(TAG, "unsuccessful response by code : ${response.code()}")
                    }
                    EspressoIdlingResource.decrement()
                }

                override fun onFailure(call: Call<CorrosionSegmentationResponse?>, t: Throwable) {
                    callback.onFailure()
                    Log.e(TAG, "api failure caused by : ${t.message}")

                    EspressoIdlingResource.decrement()
                }

            }
        )


    }

    interface CorrosionSegmentationCallback {
        fun onItemsReceived(corrosionSegmentationResponse: CorrosionSegmentationResponse?)
        fun onFailure()
    }
}