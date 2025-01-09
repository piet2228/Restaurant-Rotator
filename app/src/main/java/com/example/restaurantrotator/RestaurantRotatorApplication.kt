package com.example.restaurantrotator

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RestaurantRotatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.MAPS_API_KEY)
    }
}