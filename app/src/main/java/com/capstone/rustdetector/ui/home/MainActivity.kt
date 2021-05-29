package com.capstone.rustdetector.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.rgb
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.ml.CorrsegmUnetmodel
import com.google.android.material.appbar.AppBarLayout
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

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
            try {
                if(Build.VERSION.SDK_INT < 28) {
                    bitmapSelectedPhoto = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPhotoUri)
                    binding.imageViewPreviewUploadedImage.setImageBitmap(bitmapSelectedPhoto)
                } else {
                    val source = selectedPhotoUri?.let {
                            ImageDecoder.createSource(this.contentResolver, it)
                        }
                    bitmapSelectedPhoto = source?.let { ImageDecoder.decodeBitmap(it) }
                    bitmapSelectedPhoto = bitmapSelectedPhoto?.copy(Bitmap.Config.ARGB_8888, true)
                    binding.imageViewPreviewUploadedImage.setImageBitmap(bitmapSelectedPhoto)
                }
            }catch (e : IOException){
                Log.e(TAG, "Upload and convert image to bitmap failed : ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

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
        // accelerate model inference
        val compatList = CompatibilityList()
        val options = if(compatList.isDelegateSupportedOnThisDevice) {
            // if the device has a supported GPU, add the GPU delegate
            Model.Options.Builder().setDevice(Model.Device.GPU).build()
        } else {
            // if the GPU is not supported, run on 4 threads
            Model.Options.Builder().setNumThreads(4).build()
        }

        // initialize model
        val model = CorrsegmUnetmodel.newInstance(applicationContext, options)

        // preparation
        bitmapSelectedPhoto = bitmapSelectedPhoto?.let {
            Bitmap.createScaledBitmap(it, 256, 256, true)
        }

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
        //val byteBufferOutput : ByteBuffer = outputFeature0.buffer
//        val imageBytes = ByteArray(byteBufferOutput.remaining())
//        byteBufferOutput[imageBytes]
//        val bitmapResult = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//        binding.imageViewResult.setImageBitmap(bitmapResult)


//        val floatArray: FloatArray = outputFeature0.floatArray
//        val outputBitmap : Bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
//
//        for (i in 0 .. floatArray.size){
//            val imageSize = 256
//            outputBitmap.setPixel(
//                i - (i / imageSize) * imageSize ,
//                i / imageSize, Color.rgb(0, 0, 0))
//        }
//        binding.imageViewResult.setImageBitmap(outputBitmap)

        val bitmapResult = floatArrayToGrayscaleBitmap(
            outputFeature0.floatArray,
            256,
            256,
            1,
            false
        )

        //val bitmapResult : Bitmap = floatArrayToBitmap(outputFeature0.floatArray, 256, 256)
        binding.imageViewResult.setImageBitmap(bitmapResult)

        // Releases model resources if no longer used.
        model.close()
    }

//    private fun getOutputImage(output : TensorBuffer): Bitmap {
//        output?.rewind() // Rewind the output buffer after running.
//
//        val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
//        val pixels = IntArray(outputWidth * outputHeight) // Set your expected output's height and width
//        for (i in 0 until outputWidth * outputHeight) {
//            val a = 0xFF
//            val r: Float = output?.float!! * 255.0f
//            val g: Float = output?.float!! * 255.0f
//            val b: Float = output?.float!! * 255.0f
//            pixels[i] = a shl 24 or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
//        }
//        bitmap.setPixels(pixels, 0, outputWidth, 0, 0, outputWidth, outputHeight)
//
//        return bitmap
//    }

    private fun floatArrayToGrayscaleBitmap (
        floatArray: FloatArray,
        width: Int,
        height: Int,
        alpha :Byte,
        reverseScale :Boolean = false
    ) : Bitmap {

        // Create empty bitmap in RGBA format (even though it says ARGB but channels are RGBA)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val byteBuffer = ByteBuffer.allocate(width*height*4)

        // mapping smallest value to 0 and largest value to 255
        val maxValue = floatArray.maxOrNull() ?: 1.0f
        val minValue = floatArray.minOrNull() ?: 0.0f
        val delta = maxValue-minValue
        var tempValue :Byte

        // Define if float min..max will be mapped to 0..255 or 255..0
        val conversion = when(reverseScale) {
            false -> { v: Float -> ((v-minValue)/delta*255).toInt().toByte() }
            true -> { v: Float -> (255-(v-minValue)/delta*255).toInt().toByte() }
        }

        // copy each value from float array to RGB channels and set alpha channel
        floatArray.forEachIndexed { i, value ->
            tempValue = conversion(value)
            byteBuffer.put(4*i, tempValue)
            byteBuffer.put(4*i+1, tempValue)
            byteBuffer.put(4*i+2, tempValue)
            byteBuffer.put(4*i+3, alpha)
        }

        bmp.copyPixelsFromBuffer(byteBuffer)

        return bmp
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}