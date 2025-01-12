package com.example.restaurantrotator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties

//Adapted from https://stackoverflow.com/questions/64419367/does-jetpack-compose-offer-a-material-autocomplete-textview-replacement
@Composable
fun TextFieldWithDropdown(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    setValue: (TextFieldValue) -> Unit,
    onItemSelect: (String, Int) -> Unit,
    onDismissRequest: () -> Unit,
    dropDownExpanded: Boolean,
    list: List<String>,
    label: String = ""
) {
    Box(modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused)
                        onDismissRequest()
                },
            value = value,
            onValueChange = setValue,
            label = { Text(label) },
        )
        DropdownMenu(
            expanded = dropDownExpanded,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismissRequest
        ) {
            list.forEachIndexed { i, text ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelect(text, i)
                    },
                    text = {Text(text)}
                )
            }
        }
    }
}

@Preview
@Composable
fun preview() {
    val all = listOf("aaa", "baa", "aab", "abb", "bab")

    val dropDownOptions = remember{mutableStateOf(listOf<String>())}
    val textFieldValue = remember{mutableStateOf(TextFieldValue())}
    val dropDownExpanded = remember{mutableStateOf(false)}
    TextFieldWithDropdown(
        value = textFieldValue.value,
        modifier = Modifier.fillMaxWidth(),
        setValue = {
            textFieldValue.value = it
            dropDownExpanded.value = true
       },
        onItemSelect = {str, i->
            textFieldValue.value = TextFieldValue(str, TextRange(str.length))
            dropDownExpanded.value = true
        },
        onDismissRequest = {dropDownExpanded.value = false},
        dropDownExpanded = dropDownExpanded.value,
        list = listOf("AAA", "BBB", "CCC"),
        label = "LABEL",
    )
}
//TODO keep it in focus