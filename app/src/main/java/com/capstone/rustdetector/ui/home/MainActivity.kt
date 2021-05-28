package com.capstone.rustdetector.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.capstone.rustdetector.FunctionUtil
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    // replacement for onActivityResult
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val dataIntent: Intent? = result.data
            binding.imageViewPreviewUploadedImage.setImageURI(dataIntent?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbarTitle()
        setComponentEvent()
    }

    private fun setComponentEvent(){
        binding.floatingButtonUploadImage.setOnClickListener { selectImage() }
        binding.buttonGetResult.setOnClickListener { getSegmentationResult() }
    }

    private fun setToolbarTitle(){
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

        // startIntentForResult deprecated. diganti ini
        resultLauncher.launch(selectImageIntent)
    }

    private fun getSegmentationResult(){
        FunctionUtil.makeToast(this@MainActivity, "get segmentation result")
    }
}