package com.capstone.rustdetector.utils

import android.support.test.espresso.idling.CountingIdlingResource

class EspressoIdlingResource {
    companion object{
        private const val resource = "GLOBAL"
        private val espressoTestIdlingResource = CountingIdlingResource(resource)

        fun increment() {
            espressoTestIdlingResource.increment()
        }

        fun decrement() {
            espressoTestIdlingResource.decrement()
        }

    }
}