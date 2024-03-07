package com.example.myapp.todo.ui.item

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.todo.util.showSimpleNotificationWithTapAction
import com.example.myapp.core.Result
import com.example.myapp.todo.util.MyMap
import com.example.myapp.todo.util.createNotificationChannel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val localContext = LocalContext.current

    val channelId = "MyTestChannel"
    LaunchedEffect(Unit) {
        createNotificationChannel(channelId, localContext);
    }

    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState
    var text by rememberSaveable { mutableStateOf(itemUiState.device.text) }
    var price by rememberSaveable { mutableStateOf(itemUiState.device.price) }
    var date by rememberSaveable { mutableStateOf(itemUiState.device.date) }
    var inStock by rememberSaveable { mutableStateOf(itemUiState.device.inStock) }
    var lat by rememberSaveable { mutableStateOf(itemUiState.device.lat) }
    var lon by rememberSaveable { mutableStateOf(itemUiState.device.lon) }
    Log.d("ItemScreen", "recompose, text = $text")

    val onLocationChanged: (Double, Double) -> Unit = { newlat, newlon ->
        Log.d("ItemScreen", "onLocationChanged, lat = $lat, lon = $lon")
        lat = newlat
        lon = newlon
    }

    LaunchedEffect(itemUiState.submitResult) {
        Log.d("ItemScreen", "Submit = ${itemUiState.submitResult}");
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen", "Closing screen");
            onClose();
        }
    }

    var textInitialized by remember { mutableStateOf(itemId == null) }
    var priceInitialized by remember { mutableStateOf(itemId == null) }
    var dateInitialized by remember { mutableStateOf(itemId == null) }
    var inStockInitialized by remember { mutableStateOf(itemId == null) }
    var latInitialized by remember { mutableStateOf(itemId == null) }
    var lonInitialized by remember { mutableStateOf(itemId == null) }

    LaunchedEffect(itemId, itemUiState.loadResult) {
        Log.d("ItemScreen", "Text initialized = ${itemUiState.loadResult}");
        if (textInitialized && priceInitialized && dateInitialized && inStockInitialized && latInitialized && lonInitialized) {
            return@LaunchedEffect
        }
        if (!(itemUiState.loadResult is Result.Loading)) {
            text = itemUiState.device.text
            price = itemUiState.device.price
            date = itemUiState.device.date
            inStock = itemUiState.device.inStock
            lat = itemUiState.device.lat
            lon = itemUiState.device.lon
            textInitialized = true
            priceInitialized = true
            dateInitialized = true
            inStockInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Device") },
                actions = {
                    Button(onClick = {
                        Log.d("ItemScreen", "save item text = $text");
                        itemViewModel.saveOrUpdateItem(text, price, date, inStock, lat, lon)
                        showSimpleNotificationWithTapAction(
                            localContext,
                            "MyTestChannel",
                            0,
                            "Device Saved",
                            "The device has been successfully saved.",
                            NotificationCompat.PRIORITY_DEFAULT
                        )
                    }) { Text("Save") }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (itemUiState.loadResult is Result.Loading) {
                CircularProgressIndicator()
                return@Scaffold
            }
            if (itemUiState.submitResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { LinearProgressIndicator() }
            }
            if (itemUiState.loadResult is Result.Error) {
                Text(text = "Failed to load item - ${(itemUiState.loadResult as Result.Error).exception?.message}")
            }

            Row {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row {
                var textValue by remember { mutableStateOf(price.toString()) }

                TextField(
                    value = textValue,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue == "0" || newValue.toDoubleOrNull() != null) {
                            textValue = newValue
                            price = newValue.toDoubleOrNull() ?: 0.0
                        } else {
                            val cleanedValue = newValue.trimStart('0')
                            textValue = cleanedValue
                            price = cleanedValue.toDoubleOrNull() ?: 0.0
                        }
                    },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                )
            }


            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Release date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val datePickerDialog = DatePickerDialog(
                            localContext,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                date = "$dayOfMonth/${month + 1}/$year"
                            },
                            year,
                            month,
                            day
                        )
                        datePickerDialog.show()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Select Date")
                }
            }


            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                TextField(
                    value = if (inStock) "Yes" else "No",
                    onValueChange = { inStock = it == "Yes" },
                    label = { Text("In Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                )
                Checkbox(
                    checked = inStock,
                    onCheckedChange = { inStock = it },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Row {
                MyMap(lat = lat, lon = lon, onLocationChanged = onLocationChanged)
            }


            if (itemUiState.submitResult is Result.Error) {
                Text(
                    text = "Failed to submit item - ${(itemUiState.submitResult as Result.Error).exception?.message}",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = "0", onClose = {})
}
