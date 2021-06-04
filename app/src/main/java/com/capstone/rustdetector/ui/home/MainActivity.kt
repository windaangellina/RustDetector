package com.capstone.rustdetector.ui.home

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.utils.FunctionUtil.makeToast
import com.capstone.rustdetector.utils.FunctionUtil.showImageByBitmapUsingGlide
import com.capstone.rustdetector.utils.FunctionUtil.showImageByUrlUsingGlide
import com.capstone.rustdetector.viewmodel.RustDetectorViewModel
import com.google.android.material.appbar.AppBarLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    // view model
    private val rustDetectorViewModel : RustDetectorViewModel by viewModel()

    // data
    private var bitmapSelectedPhoto : Bitmap? = null

    companion object{
        private const val TAG = "MainActivity"
    }

    // replacement for onActivityResult
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val dataIntent: Intent? = result.data

            val selectedPhotoUri : Uri? = dataIntent?.data
            bitmapSelectedPhoto = getBitmapFromUri(selectedPhotoUri)
            bitmapSelectedPhoto?.let { showImageByBitmapUsingGlide(applicationContext, it, binding
                .imageViewPreviewUploadedImage) }
        }
    }

    private fun getBitmapFromUri(uri : Uri?) : Bitmap?{
        var bitmap
                : Bitmap? = null
        try {
            if(Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver, uri
                )
            } else {
                val source =
                    uri?.let {
                        ImageDecoder.createSource(this.contentResolver,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        setCollapseToolbarTitle()
        setComponentInitialConditions()
        setComponentEvent()
    }

    private fun setComponentEvent(){
        binding.floatingButtonUploadImage.setOnClickListener { selectImage() }
        binding.buttonGetResult.setOnClickListener { observeSegmentationResult() }
        binding.buttonDownloadResult.setOnClickListener { download() }
    }

    private fun setComponentInitialConditions(){
        binding.progressBar.visibility = View.GONE
        observeLoadingStatus()
    }

    private fun setCollapseToolbarTitle(){
        var isShow = true
        var scrollRange = -1
        val appBarLayout = binding.appbar

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1){
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0){
                binding.collapsingToolbar.title = getString(R.string.app_name)
                isShow = true
            } else if (isShow){
                binding.collapsingToolbar.title = " "
                isShow = false
            }
        })
    }

    private fun selectImage(){
        val selectImageIntent = Intent(Intent.ACTION_GET_CONTENT)
        selectImageIntent.type = "image/*"

        // replacement for deprecated startIntentForResult
        resultLauncher.launch(selectImageIntent)
    }

    private fun download(){
        val cw = ContextWrapper(applicationContext)
        val directory: File = cw.getDir("imageDir", MODE_PRIVATE)
        val file = File(directory, "UniqueFileName" + ".jpg")
        if (!file.exists()) {
            Log.d("path", file.toString())
            var fos: FileOutputStream? = null
            fos = FileOutputStream(file)
            fos.flush()
            fos.close()
        }
    }

    private fun observeLoadingStatus(){
        rustDetectorViewModel.loadingStatus.observe(this, {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun observeSegmentationResult(){
        bitmapSelectedPhoto?.let { bitmap ->
            rustDetectorViewModel.getCorrosionSegmentationResult(bitmap).observe(this, { event ->
                event.getContentIfNotHandled().let {
                    if (it != null){
                        val imageUrl = it.url
                        showImageByUrlUsingGlide(
                            applicationContext, imageUrl, binding.imageViewResult
                        )
                    }
                    else{
                        makeToast(applicationContext, "null segmentation response")
                    }
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}