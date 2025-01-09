package com.example.restaurantrotator.repository

import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(private val placesClient: PlacesClient) :
    PlaceRepository {
    override suspend fun getAutoCompletePredictions(input: String): List<AutocompletePrediction> {
        val center = LatLng(49.25, -123.11)
        val circle = CircularBounds.newInstance(center, /* radius = */ 5000.0);
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(input)
            .setLocationRestriction(circle)
            .build()
        try {
            val response = placesClient.findAutocompletePredictions(request).await()
            return response.autocompletePredictions
        } catch (e: ApiException) {
            // Handle Places API specific errors
            Log.e("PlacesRepository", "Places API error: ${e.statusCode}", e)
        } catch (e: IOException) {
            // Handle network errors
            Log.e("PlacesRepository", "Network error", e)
        } catch (e: Exception) {
            // Handle other errors
            Log.e("PlacesRepository", "Unexpected error", e)
        }
        return emptyList()
    }

    override suspend fun getAutoCompletePredictions(
        input: String,
        location: LatLng
    ): List<AutocompletePrediction> {
        val circle = CircularBounds.newInstance(location, /* radius = */ 5000.0);
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(input)
            .setLocationRestriction(circle)
            .build()
        try {
            val response = placesClient.findAutocompletePredictions(request).await()
            return response.autocompletePredictions
        } catch (e: ApiException) {
            // Handle Places API specific errors
            Log.e("PlacesRepository", "Places API error: ${e.statusCode}", e)
            emptyList<AutocompletePrediction>()
        } catch (e: IOException) {
            // Handle network errors
            Log.e("PlacesRepository", "Network error", e)
            emptyList<AutocompletePrediction>()
        } catch (e: Exception) {
            // Handle other errors
            Log.e("PlacesRepository", "Unexpected error", e)
        }
        return emptyList()
    }

}