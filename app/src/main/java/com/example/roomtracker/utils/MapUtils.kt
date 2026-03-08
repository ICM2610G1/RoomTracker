package com.example.roomtracker.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

fun generateRandomLocation(bounds: LatLngBounds): LatLng {

    val lat = bounds.southwest.latitude +
            Math.random() * (bounds.northeast.latitude - bounds.southwest.latitude)

    val lng = bounds.southwest.longitude +
            Math.random() * (bounds.northeast.longitude - bounds.southwest.longitude)

    return LatLng(lat, lng)
}

fun distanceMeters(a: LatLng, b: LatLng): Double {

    val results = FloatArray(1)

    Location.distanceBetween(
        a.latitude,
        a.longitude,
        b.latitude,
        b.longitude,
        results
    )

    return results[0].toDouble()
}