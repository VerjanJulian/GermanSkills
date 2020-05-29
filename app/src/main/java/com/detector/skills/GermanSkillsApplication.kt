package com.detector.skills

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GermanSkillsApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GermanSkillsApplication)
            modules(appModules)
        }
    }
}