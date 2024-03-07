package com.example.myapp.todo.ui.items

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.todo.ui.MyNetworkStatus
import com.example.myapp.todo.ui.NetworkStatusViewModel
import com.ilazar.mysensorsapp.TemperatureSensor
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(onItemClick: (id: String?) -> Unit, onAddItem: () -> Unit, onLogout: () -> Unit) {
    Log.d("ItemsScreen", "recompose")
    var isStatusClicked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    suspend fun showStatus() {
        if(!isStatusClicked) {
            isStatusClicked = true
            delay(3000L)
            isStatusClicked = false
        }
    }
    val myNetworkStatusViewModel = viewModel<NetworkStatusViewModel>(
        factory = NetworkStatusViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
    val itemsViewModel = viewModel<ItemsViewModel>(factory = ItemsViewModel.Factory)
    val itemsUiState by itemsViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = listOf()
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // First Row - "Devices" and Network Status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Devices")
                            Spacer(modifier = Modifier.padding(8.dp))
                            MyNetworkStatus(
                                myNetworkStatusViewModel,
                                onClick = {
                                    coroutineScope.launch {
                                        showStatus()
                                    }
                                })
                        }
                        Row() {
                            TemperatureSensor()
                        }
                    }
                },
                actions = {
                    Button(onClick = {
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }
            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("ItemsScreen", "add")
                    onAddItem()
                },
            ) { Icon(Icons.Rounded.Add, "Add") }
        }
    ) {
        padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Network status message should be at the top of the Column
            NetworkStatusMessage(shown = isStatusClicked, networkStatus = myNetworkStatusViewModel)

            // Item list below the network status message
            ItemList(
                deviceList = itemsUiState,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
fun PreviewItemsScreen() {
    ItemsScreen(onItemClick = {}, onAddItem = {}, onLogout = {})
}

@Composable
private fun NetworkStatusMessage(shown : Boolean, networkStatus : NetworkStatusViewModel) {

    AnimatedVisibility(
        visible = shown,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            color = if (networkStatus.uiState) Color.Green else Color.Red,
            tonalElevation = 6.dp

        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            )
            {
                Text(
                    text = if (networkStatus.uiState) stringResource(R.string.internet_stable_message) else stringResource(
                        R.string.internet_unstable_message
                    ),
                    modifier = Modifier.padding(8.dp),

                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 2f,
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f)
                        )
                    )
                )
            }
        }
    }
}
