package com.example.myapp.todo.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.myapp.todo.util.ConnectivityManagerNetworkMonitor
import com.example.myapp.todo.util.MyWorker
import kotlinx.coroutines.launch
import java.util.UUID

class NetworkStatusViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(false)
        private set

    private var workManager: WorkManager
    private var workId: UUID? = null

    init {
        collectNetworkStatus()
        workManager = WorkManager.getInstance(getApplication())
    }

    private fun collectNetworkStatus() {
        viewModelScope.launch {
            ConnectivityManagerNetworkMonitor(getApplication()).isOnline.collect {
                uiState = it;
            }
        }
    }

    fun startJob() {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = Data.Builder()
                .build()
            val myWork = OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            workId = myWork.id

            workManager.apply {

                enqueue(myWork)
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NetworkStatusViewModel(application)
            }
        }
    }
}


@Composable
fun MyNetworkStatus(viewModel: NetworkStatusViewModel, onClick : () -> Unit) {
    val NewtworkStatusViewModel = viewModel<NetworkStatusViewModel>(
        factory = NetworkStatusViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(Icons.Default.Face, contentDescription = "Network Status",
            tint = if (viewModel.uiState) Color.Green else Color.Red)
        Text(
            text = if (viewModel.uiState) "Online" else "Offline",
            color = if (viewModel.uiState) Color.Green else Color.Red
        )
    }

    LaunchedEffect(NewtworkStatusViewModel.uiState){
        if(NewtworkStatusViewModel.uiState){
            NewtworkStatusViewModel.startJob()
        }
    }
}
