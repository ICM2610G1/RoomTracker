package com.example.roomtracker.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.roomtracker.model.OrientationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorRepository(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gyroscope =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val magnetometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val rotationVector =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _orientation =
        MutableStateFlow(OrientationData(0f,0f,0f))

    val orientation: StateFlow<OrientationData> = _orientation

    fun start() {

        accelerometer?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        gyroscope?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        magnetometer?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        rotationVector?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {

            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            SensorManager.getRotationMatrixFromVector(
                rotationMatrix,
                event.values
            )

            SensorManager.getOrientation(
                rotationMatrix,
                orientationAngles
            )

            val azimuth =
                Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

            val pitch =
                Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

            val roll =
                Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            _orientation.value =
                OrientationData(azimuth,pitch,roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}