package com.example.restaurantrotator.di

import android.content.Context
import com.example.restaurantrotator.repository.PlaceRepository
import com.example.restaurantrotator.repository.PlaceRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient{
        return Places.createClient(context)
    }
    @Provides
    @Singleton
    fun providePlacesRepository(placesClient: PlacesClient): PlaceRepository{
        return PlaceRepositoryImpl(placesClient)
    }
}