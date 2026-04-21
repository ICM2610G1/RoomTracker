package com.example.roomtracker

import android.app.Application

class RoomTrackerApp : Application() {
    companion object {
        lateinit var appContext: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
