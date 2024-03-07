package com.ilazar.mysensorsapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TemperatureSensorMonitor(val context: Context) {
    val temperature: Flow<Float?> = callbackFlow {
        val sensorManager: SensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val temperatureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        val temperatureSensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    channel.trySend(event.values[0])
                }
            }
        }

        temperatureSensor?.let {
            sensorManager.registerListener(
                temperatureSensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        awaitClose {
            sensorManager.unregisterListener(temperatureSensorEventListener)
        }
    }
}