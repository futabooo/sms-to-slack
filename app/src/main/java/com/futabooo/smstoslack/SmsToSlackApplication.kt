package com.futabooo.smstoslack

import android.app.Application
import com.futabooo.smstoslack.di.AppContainer

class SmsToSlackApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
