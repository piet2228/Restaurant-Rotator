package com.example.restaurantrotator

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantrotator.repository.LocationRepository
import com.example.restaurantrotator.repository.PlaceRepository
import com.example.restaurantrotator.ui.TextFieldWithDropdown
import com.example.restaurantrotator.ui.theme.RestaurantRotatorTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForm : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Move API check to App class if possible
        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.MAPS_API_KEY
        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            finish()
            return
        }
        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}
@HiltViewModel
class LocationFormViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _location = MutableStateFlow<Location?>(null)
    private val _locationFail = MutableStateFlow(false)
    private val _autoCompleteSuggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    private val _searchText = MutableStateFlow(TextFieldValue())
    private val _suggestedPlace = MutableStateFlow(Place.builder().build())
    private val _expanded = MutableStateFlow(false)
    private val _cameraState = MutableStateFlow(CameraPositionState())
    private val _markerState = MutableStateFlow(MarkerState())
    private val _userLocationMarkerState = MutableStateFlow(MarkerState())
    private val _placeMarkerState = MutableStateFlow(MarkerState())
    //TODO: split markerState into suggestedMarkerState and currentMarkerState
    val location : StateFlow<Location?> = _location.asStateFlow()
    val autoCompleteSuggestions: StateFlow<List<AutocompletePrediction>> = _autoCompleteSuggestions.asStateFlow()
    val searchText = _searchText.asStateFlow()
    val suggestedPlace = _suggestedPlace.asStateFlow()
    val expanded = _expanded.asStateFlow()
    val cameraState = _cameraState.asStateFlow()
    val markerState = _markerState.asStateFlow()
    val userLocationMarkerState = _userLocationMarkerState.asStateFlow()
    val placeMarkerState = _placeMarkerState.asStateFlow()

    fun updateSearchText(textFieldValue: TextFieldValue){
        _searchText.value = textFieldValue
        viewModelScope.launch{
            launch{
                updateAutoCompleteSuggestions(textFieldValue.text)
            }
        }
        _expanded.value = true
    }
    fun onDropDownSelect(string: String, i: Int){
        updateSuggestedPlace(autoCompleteSuggestions.value[i])
        updateSearchText(TextFieldValue(string, TextRange(string.length)))
    }
    fun updateSuggestedPlace(prediction: AutocompletePrediction){
        viewModelScope.launch{
            val result = placeRepository.getPlaceDetails(prediction.placeId)
            if (result != null ){
                _suggestedPlace.value = result
                _cameraState.value = CameraPositionState(position = CameraPosition(result.latLng,15f,0f,0f))
                _placeMarkerState.value = MarkerState(position = result.latLng)
            }
        }
    }
    fun onDismiss(){
        _expanded.value = false
    }
    //Returns false if permission check fails
    fun updateLocation(){
        viewModelScope.launch{
            val newLocation  = locationRepository.getLastLocation()
            if (newLocation == null){
                _locationFail.value = true
                return@launch
            }
            _locationFail.value = false
            _location.value = newLocation
            val newLatlang = LatLng(newLocation.latitude, newLocation.longitude)
            _userLocationMarkerState.value = MarkerState(position = newLatlang)
            _cameraState.value = CameraPositionState(position =CameraPosition(newLatlang, 15f, 0f, 0f))

        }
    }
    suspend fun updateAutoCompleteSuggestions(input: String){
        if (_location.value != null){
            val results = placeRepository.getAutoCompletePredictions(input, locationToLatLng(
                _location.value!!
            ))
            _autoCompleteSuggestions.value = results
        }
        else{
            val results = placeRepository.getAutoCompletePredictions(input)
            _autoCompleteSuggestions.value = results
        }

    }
    private fun locationToLatLng(loc: Location) : LatLng{
        return LatLng(loc.latitude,loc.longitude)
    }

}
//TODO: request location perms in UI?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: LocationFormViewModel = hiltViewModel<LocationFormViewModel>()
) {
    val location = LatLng(0.0,0.0)
    val singaporeMarkerState = rememberMarkerState(position = location)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 1f)
    }
    RequestLocationPermissionUsingRememberLauncherForActivityResult({},{})
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Enter a valid location")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            var expanded = viewModel.expanded.collectAsState()
            val searchText = viewModel.searchText.collectAsState()
            val suggestions = viewModel.autoCompleteSuggestions.collectAsState()
            val suggestionsAsListOfStrings = suggestions.value
                .take(5)
                .map {
                    item ->
                    item.getPrimaryText(null).toString()
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextFieldWithDropdown(
                    modifier = Modifier.weight(7f),
                    value = searchText.value,
                    setValue = {
                        viewModel.updateSearchText(it)
                    },
                    onItemSelect = { string, i ->
                        viewModel.onDropDownSelect(string, i)
                    },
                    onDismissRequest = { viewModel.onDismiss() },
                    dropDownExpanded = expanded.value,
                    list = suggestionsAsListOfStrings,
                    label = "Address",
                )
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = viewModel.cameraState.collectAsState().value,
                ) {
                    Marker(
                        state = viewModel.placeMarkerState.collectAsState().value,
                        title = "Location of place you are adding",
                        snippet = "Enter a valid address to change"
                    )
                    Marker(
                        state = viewModel.userLocationMarkerState.collectAsState().value,
                        title = "Your current location",
                        snippet = "Adjusts auto complete results to be closer to you",
                        alpha = 0.6f
                    )
                }
                FilledIconButton(
                    onClick = {viewModel.updateLocation()},
                    modifier = Modifier.align(Alignment.TopEnd),
                    enabled = true
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_my_location_24),
                        contentDescription = "My Location"
                    )
                }

            }
        }

    }
}

@Preview
@Composable
fun GreetingPreview() {
    RestaurantRotatorTheme {
        MainContent()
    }
}