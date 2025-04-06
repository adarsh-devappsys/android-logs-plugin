package com.devappsys.logsplugin.listeners

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
class NetworkListener(
    context: Context,
    private val onNetworkAvailable: (Boolean) -> Unit
) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onNetworkAvailable(true)
        }

        override fun onLost(network: Network) {
            onNetworkAvailable(false)
        }

        override fun onUnavailable() {
            onNetworkAvailable(false)
        }
    }

    fun register() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // Already unregistered or not registered
        }
    }
}