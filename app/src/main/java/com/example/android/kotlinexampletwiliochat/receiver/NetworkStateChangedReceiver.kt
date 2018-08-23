package com.example.android.kotlinexampletwiliochat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkStateChangedReceiver : BroadcastReceiver() {
    var networkStateListener: NetworkStateListener? = null
    override fun onReceive(context: Context?, p1: Intent?) {
        if (isInternetConnectionAvailable(context)) {
            networkStateListener?.networkAvailable()
        } else {
            networkStateListener?.networkUnavailable()
        }
    }

    fun isInternetConnectionAvailable(context: Context?): Boolean {
        val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isInternetAvailable = if (activeNetwork?.isConnectedOrConnecting == null) {
            false
        } else {
            activeNetwork.isConnectedOrConnecting
        }
        return isInternetAvailable
    }

    interface NetworkStateListener {
        fun networkAvailable()
        fun networkUnavailable()
    }
}