package com.example.myapp.todo.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapp.MyApplication

class MyWorker(
    context: Context,
    val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val deviceRepository = (applicationContext as MyApplication).container.deviceRepository

        val notSaved = deviceRepository.getLocallySaved()
        Log.d("MyWorker", notSaved.toString())

        notSaved.forEach{ device ->
            if(device._id.length < 12){
                deviceRepository.save(device)
            }
            else{
                deviceRepository.update(device)
            }
        }

        return Result.success()
    }
}