package com.example.myapp.todo.data.remote

import com.example.myapp.core.data.remote.Api
import com.example.myapp.todo.data.Device
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItemService{
    @GET("/api/items/devices")
    suspend fun find(@Header("Authorization") authorization: String): List<Device>

    @GET("/api/items/device/{id}")
    suspend fun read(
        @Header("Authorization") authorization: String,
        @Path("id") itemId: String?
    ): Device

    @POST("/api/items/device")
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body device: Device
    ): Device

    @PUT("/api/items/device/{id}")
    suspend fun update(
        @Header("Authorization") authorization: String,
        @Path("id") itemId: String?,
        @Body device: Device
    ): Device
}

