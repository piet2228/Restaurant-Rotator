package com.example.restaurantrotator.repository

import android.location.Location

interface LocationRepository {
    suspend fun getLastLocation(): Location?
}