package com.example.roomtracker

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import java.util.concurrent.TimeUnit

@OptIn(SupabaseInternal::class)
val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = "https://aqlexalyccmmjspijyrz.supabase.co",
        supabaseKey = "sb_publishable_H8K0bX5q0QHgUb5td2wYwQ_rpkd_7ZL"
    ) {
        httpEngine = OkHttp.create {
            config {
                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }

        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000L
                connectTimeoutMillis = 60_000L
                socketTimeoutMillis = 60_000L
            }
        }

        install(Auth) {
            sessionManager = SharedPrefsSessionManager(RoomTrackerApp.appContext)
        }
        install(Postgrest)
        install(Storage)
    }
}
