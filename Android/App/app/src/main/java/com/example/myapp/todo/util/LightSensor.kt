package com.example.myapp.todo.util

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class LightSensorViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf<Float?>(null)
        private set

    init {
        viewModelScope.launch {
            LightSensorMonitor(getApplication()).lightData.collect { light ->
                uiState = light
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LightSensorViewModel(application)
            }
        }
    }
}
@Composable
fun LightSensor() {
    val lightSensorViewModel = viewModel<LightSensorViewModel>(
        factory = LightSensorViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
}
