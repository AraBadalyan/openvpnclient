package com.hundredstartups.openvpn

import android.app.Application
import androidx.startup.AppInitializer
import com.hundredstartups.openvpn.cache.PrefsCacheManager.initialize
import net.danlew.android.joda.JodaTimeInitializer

class OpenVpnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initialize(this)
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)
    }
}