package com.example.restaurantrotator.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place

interface PlaceRepository {
    suspend fun getAutoCompletePredictions(input: String): List<AutocompletePrediction>
    suspend fun getAutoCompletePredictions(input: String, location: LatLng): List<AutocompletePrediction>
    suspend fun getPlaceDetails(placeId: String): Place?
    suspend fun getTextSearch(input:String, location: LatLng): List<Place>
}