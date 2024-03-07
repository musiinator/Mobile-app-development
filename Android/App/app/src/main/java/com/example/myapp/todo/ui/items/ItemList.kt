package com.example.myapp.todo.ui.items

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.myapp.todo.data.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

typealias OnItemFn = (id: String?) -> Unit


fun getAddressFromLocation(latitude: Double, longitude: Double, context: Context): Address? {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                return addresses.get(0)
            }
        }
    } catch (e: IOException) {
        // Handle the exception
    }
    return null
}

@Composable
fun ItemList(deviceList: List<Device>, onItemClick: OnItemFn, modifier: Modifier) {
    Log.d("ItemList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(5.dp),
        content = {
            items(deviceList) {
                item ->
                ItemDetail(item, onItemClick)
                Divider()
            }
        }
    )
}

@Composable
fun ItemDetail(device: Device, onItemClick: OnItemFn) {
    var isLongPressed by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(isLongPressed) {
        if (isLongPressed) {
            address = withContext(Dispatchers.IO) {
                getAddressFromLocation(device.lat, device.lon, context)?.getAddressLine(0)
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { onItemClick(device._id) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isLongPressed = !isLongPressed
                    },
                    onTap = {
                        onItemClick(device._id)
                    }
                )
            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 50,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                Text(text = "Name: ")
                Text(text = device.text)
            }
            Row {
                Text(text = "Price: ")
                Text(text = device.price.toString())
            }
            Row {
                Text(text = "Release date: ")
                Text(text = device.date)
            }
            Row {
                Text(text = "In Stock: ")
                Text(text = device.inStock.toString())
            }
            AnimatedVisibility(
                visible = isLongPressed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row {
                    Text(text = "Address: ")
                    Text(text = address ?: "Loading...")
                }
            }
        }
    }
}
