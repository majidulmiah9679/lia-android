package com.gemini.multiverse.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TiltData(
    val pitch: Float = 0f, // X-axis tilt (-1.0 to 1.0)
    val roll: Float = 0f   // Y-axis tilt (-1.0 to 1.0)
)

/**
 * Monitors device accelerometer/rotation vector to provide real-time
 * 3D tilt coordinates for dynamic specular lighting on the 3D Orb.
 */
class TiltSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltData = MutableStateFlow(TiltData())
    val tiltData: StateFlow<TiltData> = _tiltData.asStateFlow()

    fun startListening() {
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // Normalize pitch and roll between -1.0 and 1.0
            val pitch = (orientation[1] / (Math.PI / 2)).toFloat().coerceIn(-1f, 1f)
            val roll = (orientation[2] / (Math.PI / 2)).toFloat().coerceIn(-1f, 1f)

            _tiltData.value = TiltData(pitch = pitch, roll = roll)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val pitch = (event.values[1] / 9.8f).coerceIn(-1f, 1f)
            val roll = (event.values[0] / 9.8f).coerceIn(-1f, 1f)
            _tiltData.value = TiltData(pitch = pitch, roll = roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
