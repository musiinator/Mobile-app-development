package com.example.myapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "items")
data class Device(@PrimaryKey var _id: String = UUID.randomUUID().toString(),
                      val text: String = "",
                      val price: Double = 0.0,
                      val date: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                      val inStock : Boolean = false,
                      var isSentToServer: Boolean=true,
                      var lat: Double=46.77331,
                      var lon: Double=23.62137
                 )
