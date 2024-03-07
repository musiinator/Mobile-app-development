package com.example.myapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapp.todo.data.Device
import com.example.myapp.todo.data.local.ItemDao

@Database(entities = [Device::class], version = 2, exportSchema = false)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        /*private val migration1to2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //delete the old table
                database.execSQL("DROP TABLE items")
                //create the new table
                database.execSQL("CREATE TABLE items (_id TEXT NOT NULL, text TEXT NOT NULL, price REAL NOT NULL, date TEXT NOT NULL, inStock INTEGER NOT NULL, isSentToServer INTEGER NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, PRIMARY KEY(_id))")
            }
        }*/

        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    //.addMigrations(migration1to2) // Add the migration here
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
