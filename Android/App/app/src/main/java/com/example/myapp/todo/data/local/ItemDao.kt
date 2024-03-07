package com.example.myapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapp.todo.data.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): Flow<List<Device>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(devices: List<Device>)

    @Update
    suspend fun update(device: Device): Int

    @Query("DELETE FROM items WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    @Query("DELETE FROM Items where text= :text and isSentToServer = :isSaved")
    suspend fun deleteItemNotSaved(text: String, isSaved: Boolean)

    @Query("SELECT * FROM Items where isSentToServer = :isSaved")
    suspend fun getLocalItems(isSaved: Boolean): List<Device>

    @Query("SELECT COUNT(*) FROM Items where isSentToServer = :isSaved")
    suspend fun getNotSaved(isSaved: Boolean): Int

}
