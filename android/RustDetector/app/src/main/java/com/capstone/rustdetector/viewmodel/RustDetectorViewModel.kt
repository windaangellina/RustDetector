package com.capstone.rustdetector.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.rustdetector.source.RustDetectorRepository
import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import com.capstone.rustdetector.utils.Event

class RustDetectorViewModel(private val repository: RustDetectorRepository) : ViewModel() {
    val loadingStatus : LiveData<Boolean> = repository.isLoading

    fun getCorrosionSegmentationResult(bitmapSelectedPhoto: Bitmap, fileName: String):
            LiveData<Event<CorrosionSegmentationResponse?>> {
        return repository.getSegmentationResult(bitmap = bitmapSelectedPhoto, fileName = fileName)

//        val testingResult = CorrosionSegmentationResponse(
//            "https://assets.pikiran-rakyat.com/crop/67x118:655x668/x/photo/2020/12/06/2809916107.png"
//        )
//        val mutable = MutableLiveData<Event<CorrosionSegmentationResponse?>>()
//        mutable.postValue(Event(testingResult))
//        return mutable
    }
}