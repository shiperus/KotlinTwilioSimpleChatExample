package com.example.android.kotlinexampletwiliochat

import android.app.Application
import android.util.Log
import com.example.android.kotlinexampletwiliochat.api.ApiHelper
import com.squareup.leakcanary.LeakCanary

class KotlinExampleTwilioChatApplication : Application() {

    var appChatClient: AppChatClient = AppChatClient(this)
        private set
    var apiHelper: ApiHelper = ApiHelper()
        private set

    override fun onCreate() {
        super.onCreate()
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return
//        }
//        LeakCanary.install(this)
    }


}