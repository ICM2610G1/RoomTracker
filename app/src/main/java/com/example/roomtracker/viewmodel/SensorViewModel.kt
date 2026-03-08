package com.example.roomtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.roomtracker.data.SensorRepository

class SensorViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository =
        SensorRepository(application)

    val orientation = repository.orientation

    fun startSensors() {
        repository.start()
    }

    fun stopSensors() {
        repository.stop()
    }
}