package com.capstone.rustdetector.source

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import com.capstone.rustdetector.utils.Event

interface RustDetectorDataSource {
    fun getSegmentationResult(bitmap: Bitmap, fileName : String) :
            LiveData<Event<CorrosionSegmentationResponse?>>
}