package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.Result
import com.example.myapp.core.TAG
import com.example.myapp.todo.data.Device
import com.example.myapp.todo.data.DeviceRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class ItemUiState(
    val itemId: String? = null,
    val device: Device = Device(),
    var loadResult: Result<Device>? = null,
    var submitResult: Result<Device>? = null,
)

class ItemViewModel(private val itemId: String?, private val deviceRepository: DeviceRepository) :
    ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Device()))
        }
    }

    fun loadItem() {
        viewModelScope.launch {
            deviceRepository.itemStream.collect { items ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                val device = items.find { it._id == itemId } ?: Device()
                uiState = uiState.copy(device = device, loadResult = Result.Success(device))
            }
        }
    }


    fun saveOrUpdateItem(text: String, price: Double, date: String, inStock: Boolean, lat: Double, lon: Double) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem...");
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val item = uiState.device.copy(text = text, price = price, date = date, inStock = inStock, lat = lat, lon = lon)
                val savedDevice: Device;
                if (itemId == null) {
                    savedDevice = deviceRepository.save(item)
                } else {
                    savedDevice = deviceRepository.update(item)
                }
                Log.d(TAG, "saveOrUpdateItem succeeeded")
                uiState = uiState.copy(submitResult = Result.Success(savedDevice))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem failed");
                val currentId = UUID.randomUUID().toString().substring(0,9);
                println("currentId: " + currentId);
                uiState = uiState.copy(submitResult = Result.Error(e))
                val device = uiState.device.copy(text = text, price = price, date = date, inStock = inStock, isSentToServer = false, lat = lat, lon = lon, _id = currentId.toString())
                deviceRepository.addLocally(device)
                Log.d(TAG, "saveOrUpdateItem ${device} added locally")
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemViewModel(itemId, app.container.deviceRepository)
            }
        }
    }
}
