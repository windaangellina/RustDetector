package com.capstone.rustdetector.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.utils.FunctionUtil
import com.capstone.rustdetector.utils.NetworkLiveData
import com.capstone.rustdetector.viewmodel.RustDetectorViewModel
import com.google.android.material.appbar.AppBarLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE: Int = 100
    }
    // view binding
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    // view model
    private val rustDetectorViewModel : RustDetectorViewModel by viewModel()

    // data
    private var selectedFileBitmap : Bitmap? = null
    private var selectedFileName : String? = null
    private var resultBitmap : Bitmap? = null

    //download
    private var outputStream: OutputStream? = null

    // replacement for deprecated onActivityResult
    private val resultLauncher = this.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val dataIntent: Intent? = result.data
            val selectedPhotoUri : Uri? = dataIntent?.data

            selectedFileBitmap = FunctionUtil.getBitmapFromUri(selectedPhotoUri, applicationContext)
            selectedFileName = FunctionUtil.getFilenameFromUri(selectedPhotoUri, applicationContext)

            selectedFileBitmap?.let {
                FunctionUtil.showImageByBitmapUsingGlide(
                    applicationContext, it, binding.layoutPrediction.imageViewPreviewUploadedImage
                )
            }
        }
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
        binding.layoutPrediction.buttonGetResult.setOnClickListener { observeSegmentationResult() }
        binding.layoutPrediction.buttonDownloadResult.setOnClickListener { download() }
    }

    private fun setComponentInitialConditions(){
        binding.layoutPrediction.progressBar.visibility = View.GONE
        checkConnection()
        observeLoadingStatus()
    }

    private fun checkConnection(){
        NetworkLiveData.init(this.application)

        // initial display set up
        if (NetworkLiveData.isNetworkAvailable()){
            setLayoutVisibility(isError = false)
        }
        else{
            setLayoutVisibility(isError = true)
        }

        // observe network state changes
        NetworkLiveData.observe(this, { event ->
            event.getContentIfNotHandled().let {
                if (it == true) {
                    //connected
                    setLayoutVisibility(isError = false)
                } else {
                    //connection gone
                    setLayoutVisibility(isError = true)
                }
            }
        })
    }

    private fun setLayoutVisibility(isError : Boolean){
        if (isError){
            binding.layoutPrediction.root.visibility = View.GONE
            binding.layoutError.root.visibility = View.VISIBLE
            binding.floatingButtonUploadImage.visibility = View.GONE
            binding.nestedScrollViewLayoutContainer.setBackgroundResource(R.drawable.bg_card_white_round_top)
        }
        else{
            binding.layoutPrediction.root.visibility = View.VISIBLE
            binding.layoutError.root.visibility = View.GONE
            binding.floatingButtonUploadImage.visibility = View.VISIBLE
            binding.nestedScrollViewLayoutContainer.setBackgroundResource(R.drawable.bg_card_white)
        }
    }

    private fun setCollapseToolbarTitle(){
        var isShow = true
        var scrollRange = -1
        val appBarLayout = binding.appbar

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                barLayout, verticalOffset ->
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
        setResultDisplay(noResult = false)

        // open gallery
        val selectImageIntent = Intent(Intent.ACTION_GET_CONTENT)
        selectImageIntent.type = "image/*"

        // replacement for deprecated startIntentForResult
        resultLauncher.launch(selectImageIntent)
    }

    private fun observeLoadingStatus(){
        rustDetectorViewModel.loadingStatus.observe(this, {
            binding.layoutPrediction.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun observeSegmentationResult(){
        selectedFileBitmap?.let { bitmap ->
            selectedFileName?.let { fileName ->
                rustDetectorViewModel.getCorrosionSegmentationResult(bitmap, fileName).observe(this,
                    { event -> event.getContentIfNotHandled().let {
                        if (it != null){
                            val imageUrl = it.url
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(imageUrl)
                                .dontAnimate()
                                .into(object : CustomTarget<Bitmap>(){
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        binding.layoutPrediction.imageViewResult.setImageBitmap(resource)
                                        resultBitmap = resource
                                    }
                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // this is called when imageView is cleared on lifecycle call or for
                                        // some other reason.
                                        // if you are referencing the bitmap somewhere else too other than this imageView
                                        // clear it here as you can no longer have the bitmap
                                    }
                                })
                        } else{
                            setResultDisplay(noResult = true)
                            FunctionUtil.makeToast(
                                applicationContext,
                                "sorry, we're unable to process your request right now"
                            )
                        }
                    }
                })
            }
        }
    }

    private fun setResultDisplay(noResult : Boolean){
        if (noResult){
            binding.layoutPrediction.imageViewResult
                .setImageResource(R.drawable.ic_undraw_feeling_blue_4b7q)
        }
        else{
            binding.layoutPrediction.imageViewResult
                .setImageResource(R.drawable.ic_undraw_photograph_re_up3b)
        }
    }

    private fun download(){
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            saveImage()
        } else {
            askPermission()
        }
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please provide the required permissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun saveImage() {
        val dir : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!dir.exists()) {
            dir.mkdir()
        }

        val bitmap = resultBitmap
        val file = File(dir, selectedFileName!!)
        try {
            outputStream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            Log.e("MainActivity", "save image failed : ${e.message}")
        }

        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        FunctionUtil.makeToast(applicationContext, "Successfully saved to Pictures")

        try {
            outputStream!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MainActivity", "save image failed : ${e.message}")
        }
        try {
            outputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MainActivity", "save image failed : ${e.message}")
        }

        scanFile(file.absolutePath)
    }

    private fun scanFile(pathLocation : String) {
        MediaScannerConnection.scanFile(
            applicationContext, arrayOf(pathLocation), null
        ) { path, uri ->
            Log.d(
                "MainActivity", "Scan finished. You can view the image in the gallery now."
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        selectedFileName = null
        selectedFileBitmap = null
        resultBitmap = null
    }
}