package com.ilazar.mysensorsapp

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class TemperatureSensorViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf<Float?>(null)
        private set

    init {
        viewModelScope.launch {
            TemperatureSensorMonitor(getApplication()).temperature.collect { temperature ->
                uiState = temperature
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TemperatureSensorViewModel(application)
            }
        }
    }
}

@Composable
fun TemperatureSensor() {
    val temperatureSensorViewModel = viewModel<TemperatureSensorViewModel>(
        factory = TemperatureSensorViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
    Text(
        text = "Current temperature: ${temperatureSensorViewModel.uiState?.toString() ?: "N/A"} °C",
        fontSize = 14.sp, // Increase font size
    )
}
