package com.example.mobildrontesztprojekt

import android.app.Application
import android.content.Context
import com.cySdkyc.clx.Helper // Ez a DJI V5 belső helper könyvtára

class MyDroneApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Ez a sor ELENGEDHETETLEN a VerifyError ellen
        Helper.install(this)
    }
}
