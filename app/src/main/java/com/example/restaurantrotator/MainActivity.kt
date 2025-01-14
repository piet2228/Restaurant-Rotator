@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.restaurantrotator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestaurantForm()
        }
    }
}

data class OperatingHours(
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime
)

data class RestaurantFormData(
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val weeklySchedule: List<OperatingHours> = DayOfWeek.entries.map { dayOfWeek ->
        OperatingHours(dayOfWeek, LocalTime.MIN, LocalTime.MAX)
    }
)

class RestaurantViewModel : ViewModel() {
    private val _restaurantData = MutableStateFlow(RestaurantFormData())
    val restaurantData: StateFlow<RestaurantFormData> = _restaurantData.asStateFlow()
    private val _displayTimeDialog = MutableStateFlow(false)
    private val _dialogForOpen = MutableStateFlow(false)
    private val _dialogDay = MutableStateFlow(DayOfWeek.MONDAY)

    val displayTimeDialog: StateFlow<Boolean> = _displayTimeDialog.asStateFlow()
    val dialogDay: StateFlow<DayOfWeek> = _dialogDay.asStateFlow()

    fun updateName(name: String) {
        _restaurantData.value = _restaurantData.value.copy(name = name)
    }

    fun updateAddress(address: String) {
        _restaurantData.value = _restaurantData.value.copy(address = address)
    }

    fun updateDescription(description: String) {
        _restaurantData.value = _restaurantData.value.copy(description = description)
    }

    fun updateRating(rating: Float) {
        _restaurantData.value = _restaurantData.value.copy(rating = rating)
    }

    fun updateWeeklySchedule(weeklySchedule: List<OperatingHours>) {
        _restaurantData.value = _restaurantData.value.copy(weeklySchedule = weeklySchedule)
    }
    fun openTimeDialog(dayOfWeek: DayOfWeek, changeOpeningTime: Boolean){
        _displayTimeDialog.value = true
        _dialogDay.value = dayOfWeek
        _dialogForOpen.value = changeOpeningTime
        val hours = restaurantData.value.weeklySchedule[dayOfWeek.ordinal]
    }
    fun confirmTimeDialog(timePickerState: TimePickerState){
        val weeklySchedule = restaurantData.value.weeklySchedule
        if (_dialogForOpen.value) {
            val oldHours = restaurantData.value.weeklySchedule[_dialogDay.value.ordinal]
            val newHours = oldHours.copy(openTime = LocalTime.of(timePickerState.hour, timePickerState.minute))
            val newSchedule = updateElementInList(weeklySchedule,dialogDay.value.ordinal, newHours)
            updateWeeklySchedule(newSchedule)
        }
        else {
            val oldHours = restaurantData.value.weeklySchedule[_dialogDay.value.ordinal]
            val newHours = oldHours.copy(closeTime = LocalTime.of(timePickerState.hour, timePickerState.minute))
            val newSchedule = updateElementInList(weeklySchedule,dialogDay.value.ordinal, newHours)
            updateWeeklySchedule(newSchedule)
        }
        _displayTimeDialog.value = false
    }
    fun dismissTimeDialog(){
        _displayTimeDialog.value = false
    }
}

//Return a copy of the list with the indexed element replaced by newValue
fun <T> updateElementInList(list: List<T>, index: Int, newValue: T): List<T> {
    return list.mapIndexed { i, element ->
        if (i == index) newValue else element
    }
}

//Return a copy of weeklySchedule with openTime and closeTime sourced from a timePickerState
fun updateWeeklyScheduleFromPicker(
    weeklySchedule: List<OperatingHours>,
    dayOfWeek: DayOfWeek,
    openPickerState: TimePickerState,
    closePickerState: TimePickerState
): List<OperatingHours> {
    val openTime = LocalTime.of(openPickerState.hour, openPickerState.minute)
    val closeTime = LocalTime.of(closePickerState.hour, closePickerState.minute)
    val newOperatingHours = OperatingHours(dayOfWeek, openTime, closeTime)
    val newWeeklySchedule =
        updateElementInList(weeklySchedule, dayOfWeek.ordinal, newOperatingHours)
    return newWeeklySchedule
}

@Composable
fun SchedulePicker(viewModel: RestaurantViewModel) {
    val uiState = viewModel.restaurantData.collectAsState()
    val weeklySchedule = uiState.value.weeklySchedule
    Column {
        //first row with headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(0.7f))
            Text(
                text = "Opening Time",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Closing Time",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        //buttons to change operating hours for every day
        ChangeAllButtons(weeklySchedule, viewModel)
        weeklySchedule.forEach{ day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.dayOfWeek.name,
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center
                )
                FilledTonalButton(
                    onClick = { viewModel.openTimeDialog(day.dayOfWeek,true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "${day.openTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
                FilledTonalButton(
                    onClick = { viewModel.openTimeDialog(day.dayOfWeek, false)},
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "${day.closeTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
            }
        }
        //TODO: Gemini lied. use the normal way to keep timepicker state, then update the viewmodel anytime it changes?
        if (viewModel.displayTimeDialog.collectAsState().value){
            val timePickerState = rememberTimePickerState(0,0,false)
            TimePickerDialog(
                onDismiss = {viewModel.dismissTimeDialog()},
                onConfirm = {viewModel.confirmTimeDialog(timePickerState)},
            ) {
                TimePicker(state=timePickerState)
            }
        }
    }
}

@Composable
private fun ChangeAllButtons(
    weeklySchedule: List<OperatingHours>,
    viewModel: RestaurantViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val displayOpeningTimeDialog = remember { mutableStateOf(false) }
        val displayClosingTimeDialog = remember { mutableStateOf(false) }
        val openingPickerState = rememberTimePickerState()
        val closingPickerState = rememberTimePickerState()

        Spacer(modifier = Modifier.weight(0.7f))
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { displayOpeningTimeDialog.value = true }) {
            Text("Set All Open")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { displayClosingTimeDialog.value = true }) {
            Text("Set All Close")
        }
        if (displayOpeningTimeDialog.value) {
            TimePickerDialog(
                onDismiss = { displayOpeningTimeDialog.value = false },
                onConfirm = {
                    val newWeeklySchedule = DayOfWeek.entries.map { dayOfWeek ->
                        OperatingHours(
                            dayOfWeek,
                            LocalTime.of(openingPickerState.hour, openingPickerState.minute),
                            weeklySchedule[dayOfWeek.ordinal].closeTime
                        )
                    }
                    viewModel.updateWeeklySchedule(newWeeklySchedule)
                    displayOpeningTimeDialog.value = false
                },
                content = { TimePicker(state = openingPickerState) }
            )
        }
        if (displayClosingTimeDialog.value) {
            TimePickerDialog(
                onDismiss = { displayClosingTimeDialog.value = false },
                onConfirm = {
                    val newWeeklySchedule = DayOfWeek.entries.map { dayOfWeek ->
                        OperatingHours(
                            dayOfWeek,
                            weeklySchedule[dayOfWeek.ordinal].openTime,
                            LocalTime.of(closingPickerState.hour, closingPickerState.minute),
                        )
                    }
                    viewModel.updateWeeklySchedule(newWeeklySchedule)
                    displayClosingTimeDialog.value = false
                },
                content = { TimePicker(state = closingPickerState) }
            )
        }
    }
}


@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                println("HI")
            }) {
                Text("Confirm")
            }
        },
        text = { content() }
    )
}

@Composable
fun RestaurantForm(viewModel: RestaurantViewModel = viewModel()) {
    val uiState = viewModel.restaurantData.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Restaurant Entry Form") }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) { }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            TextField(
                value = uiState.value.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Restaurant Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Rating: ${uiState.value.rating} out of 5",
                modifier = Modifier.fillMaxWidth(1f),
                textAlign = TextAlign.Center
            )
            Slider(
                value = uiState.value.rating,
                valueRange = 0f..5f,
                steps = 9,
                onValueChange = { viewModel.updateRating(it) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            TextField(
                value = uiState.value.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                singleLine = false,
                minLines = 5,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(1f)
            )
            SchedulePicker(viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    RestaurantForm()
}