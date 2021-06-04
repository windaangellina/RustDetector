package com.capstone.rustdetector.source.remote.api

import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CorrosionSegmentationApi {
    @POST("predict")
    fun getCorrosionSegmentationResult(
        @Body requestBody: RequestBody
    ): Call<CorrosionSegmentationResponse?>
}