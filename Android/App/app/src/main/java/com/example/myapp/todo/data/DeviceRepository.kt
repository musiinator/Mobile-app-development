package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.core.TAG
import com.example.myapp.core.data.remote.Api
import com.example.myapp.todo.data.local.ItemDao
import com.example.myapp.todo.data.remote.ItemEvent
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class DeviceRepository(
    private val itemService: ItemService,
    private val itemWsClient: ItemWsClient,
    private val itemDao: ItemDao
) {
    val itemStream by lazy { itemDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val items = itemService.find(authorization = getBearerToken())
            itemDao.deleteAll()
            items.forEach { itemDao.insert(it) }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it.isSuccess) {
                    val itemEvent = it.getOrNull();
                    when (itemEvent?.type) {
                        "created" -> handleItemCreated(itemEvent.payload)
                        "updated" -> handleItemUpdated(itemEvent.payload)
                        "deleted" -> handleItemDeleted(itemEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<kotlin.Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(device: Device): Device {
        Log.d(TAG, "update $device...")
        val updatedItem =
            itemService.update(itemId = device._id, device = device, authorization = getBearerToken())
        Log.d(TAG, "update $device succeeded")
        handleItemUpdated(updatedItem)
        return updatedItem
    }

    suspend fun save(device: Device): Device {
        Log.d(TAG, "save $device...")
        device.isSentToServer = true
        val createdItem = itemService.create(device = device, authorization = getBearerToken())
        Log.d(TAG, "save $device succeeded")
        handleItemCreated(createdItem)
        return createdItem
    }

    suspend fun addLocally(device: Device) {
        Log.d(TAG, "addLocally $device...")
        itemDao.insert(device)
    }

    suspend fun getLocallySaved(): List<Device> {
        Log.d(TAG, "getLocallySaved...")
        return itemDao.getLocalItems(isSaved = false);
    }

    private suspend fun handleItemDeleted(device: Device) {
        Log.d(TAG, "handleItemDeleted - todo $device")
    }

    private suspend fun handleItemUpdated(device: Device) {
        Log.d(TAG, "handleItemUpdated...")
        itemDao.update(device)
    }

    private suspend fun handleItemCreated(device: Device) {
        Log.d(TAG, "handleItemCreated...")
        itemDao.insert(device)
    }

    suspend fun deleteAll() {
        itemDao.deleteAll()
    }

    suspend fun getNrUnsaved(): Int {
        return itemDao.getNotSaved(false)
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }
}