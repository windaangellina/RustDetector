package com.capstone.rustdetector.ui.home

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.capstone.rustdetector.utils.FunctionUtil
import com.capstone.rustdetector.utils.NetworkLiveData
import com.capstone.rustdetector.viewmodel.RustDetectorViewModel
import com.google.android.material.appbar.AppBarLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    // view binding
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    // view model
    private val rustDetectorViewModel : RustDetectorViewModel by viewModel()

    // data
    private var selectedFileBitmap : Bitmap? = null
    private var selectedFileName : String? = null

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
                    { event ->
                    event.getContentIfNotHandled().let {
                        if (it != null){
                            val imageUrl = it.url
//                            FunctionUtil.showImageByUrlUsingGlide(
//                                applicationContext,
//                                imageUrl,
//                                binding.layoutPrediction.imageViewResult
//                            )

                            FunctionUtil.makeToast(applicationContext, imageUrl)
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(imageUrl)
                                .into(object : CustomTarget<Bitmap>(){
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        binding.layoutPrediction.imageViewResult.setImageBitmap(resource)
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
        val cw = ContextWrapper(applicationContext)
        val directory: File = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        val file = File(directory, selectedFileName)
        if (!file.exists()) {
            Log.d("path", file.toString())
            var fos: FileOutputStream? = null
            fos = FileOutputStream(file)
            fos.flush()
            fos.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        selectedFileName = null
        selectedFileBitmap = null
    }
}