package com.example.restaurantrotator.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction

interface PlaceRepository {
    suspend fun getAutoCompletePredictions(input: String): List<AutocompletePrediction>
    suspend fun getAutoCompletePredictions(input: String, location: LatLng): List<AutocompletePrediction>
}