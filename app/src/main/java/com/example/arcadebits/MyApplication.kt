package com.example.arcadebits

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()


        val optionsB = FirebaseOptions.Builder()
            .setProjectId("prieba-fin-uv5zqf")
            .setApplicationId("1:486266984865:android:5cffb341e171ab9147d3e0")
            .setApiKey("AIzaSyCJDeMYYT7C-N2U_iSSSloQ5_kugnXps1g")
            .setStorageBucket("prieba-fin-uv5zqf.firebasestorage.app")

            .build()


        if (FirebaseApp.getApps(this).none { it.name == "appB" }) {
            FirebaseApp.initializeApp(this, optionsB, "appB")
        }
    }
}