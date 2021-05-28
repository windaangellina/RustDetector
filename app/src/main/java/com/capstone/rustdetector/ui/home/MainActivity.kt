package com.capstone.rustdetector.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.ml.CorrsegmUnetmodel
import com.google.android.material.appbar.AppBarLayout
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var bitmapSelectedPhoto : Bitmap? = null

    companion object{
        private const val TAG = "MainActivity"
    }

    // replacement for onActivityResult
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val dataIntent: Intent? = result.data

            val selectedPhotoUri : Uri? = dataIntent?.data
            try {
//                binding.imageViewPreviewUploadedImage.setImageURI(selectedPhotoUri)
//                bitmapSelectedPhoto = MediaStore.Images.Media.getBitmap(
//                    contentResolver,
//                    selectedPhotoUri
//                )

                if(Build.VERSION.SDK_INT < 28) {
                    bitmapSelectedPhoto = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        selectedPhotoUri
                    )
                    binding.imageViewPreviewUploadedImage.setImageBitmap(bitmapSelectedPhoto)
                } else {
                    val source =
                        selectedPhotoUri?.let {
                            ImageDecoder.createSource(this.contentResolver,
                                it
                            )
                        }
                    bitmapSelectedPhoto = source?.let { ImageDecoder.decodeBitmap(it) }
                    bitmapSelectedPhoto = bitmapSelectedPhoto?.copy(Bitmap.Config.ARGB_8888, true)
                    binding.imageViewPreviewUploadedImage.setImageBitmap(bitmapSelectedPhoto)
                }

            }catch (e : IOException){
                Log.e(TAG, "Upload and convert image failed : ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCollapseToolbarTitle()
        setComponentEvent()
    }

    private fun setComponentEvent(){
        binding.floatingButtonUploadImage.setOnClickListener { selectImage() }
        binding.buttonGetResult.setOnClickListener { getSegmentationResult() }
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

    private fun getSegmentationResult(){
        // FunctionUtil.makeToast(this@MainActivity, "get segmentation result")

        val model = CorrsegmUnetmodel.newInstance(applicationContext)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)

        // Creates ByteBuffer
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmapSelectedPhoto)
        val byteBufferInput : ByteBuffer = tensorImage.buffer

        inputFeature0.loadBuffer(byteBufferInput)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Shows result
        val byteBufferOutput : ByteBuffer = outputFeature0.buffer
        val imageBytes = ByteArray(byteBufferOutput.remaining())
        byteBufferOutput[imageBytes]
        val bitmapResult = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.imageViewResult.setImageBitmap(bitmapResult)

        // Releases model resources if no longer used.
        model.close()
    }
}