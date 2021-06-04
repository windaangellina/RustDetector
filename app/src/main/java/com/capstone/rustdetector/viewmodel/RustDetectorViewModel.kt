package com.capstone.rustdetector.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.capstone.rustdetector.source.RustDetectorRepository
import com.capstone.rustdetector.source.remote.api.response.CorrosionSegmentationResponse
import com.capstone.rustdetector.utils.Event
import com.google.firebase.auth.FirebaseAuth

class RustDetectorViewModel(private val repository: RustDetectorRepository) : ViewModel() {
    val loadingStatus : LiveData<Boolean> = repository.isLoading

    fun getCorrosionSegmentationResult(bitmapSelectedPhoto: Bitmap) : LiveData<Event<CorrosionSegmentationResponse?>> {
        return repository.getSegmentationResult(bitmap = bitmapSelectedPhoto)
    }
}