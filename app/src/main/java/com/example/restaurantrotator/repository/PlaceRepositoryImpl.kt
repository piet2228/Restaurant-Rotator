package com.example.restaurantrotator.repository

import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
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
            Log.e("PlacesRepository", "Places API error: ${e.statusCode}", e)
        } catch (e: IOException) {
            Log.e("PlacesRepository", "Network error", e)
        } catch (e: Exception) {
            Log.e("PlacesRepository", "Unexpected error", e)
        }
        return emptyList()
    }

    override suspend fun getPlaceDetails(placeId: String): Place? {
        val placeFields = listOf<Place.Field>(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        try {
            val response = placesClient.fetchPlace(request).await()
            return response.place
        } catch (e: ApiException) {
            Log.e("PlacesRepository", "Places API error: ${e.statusCode}", e)
        } catch (e: IOException) {
            Log.e("PlacesRepository", "Network error", e)
        } catch (e: Exception) {
            Log.e("PlacesRepository", "Unexpected error", e)
        }
        return null;
    }

    override suspend fun getTextSearch(input: String, location: LatLng): List<Place> {
        val placeFields = listOf<Place.Field>(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val circle = CircularBounds.newInstance(location, 5000.0)
        val request = SearchByTextRequest.builder(input, placeFields).setLocationBias(circle).setMaxResultCount(10).build()
        try {
            val response = placesClient.searchByText(request).await()
            return response.places
        } catch (e: ApiException) {
            Log.e("PlacesRepository", "Places API error: ${e.statusCode}", e)
        } catch (e: IOException) {
            Log.e("PlacesRepository", "Network error", e)
        } catch (e: Exception) {
            Log.e("PlacesRepository", "Unexpected error", e)
        }
        return emptyList()
    }


}