package com.example.restaurantrotator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.restaurantrotator.ui.theme.RestaurantRotatorTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.MutableStateFlow

class LocationForm : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}
class locationViewModel: ViewModel(){
    val location = MutableStateFlow(LatLng(0.0,0.0))
}
@Composable
fun MainContent() {
    val location = LatLng(0.0,0.0)
    val singaporeMarkerState = rememberMarkerState(position = location)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 1f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = singaporeMarkerState,
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RestaurantRotatorTheme {
        MainContent()
    }
}