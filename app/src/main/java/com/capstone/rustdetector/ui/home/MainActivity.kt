package com.capstone.rustdetector.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.capstone.rustdetector.R
import com.capstone.rustdetector.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbarTitle()
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
}