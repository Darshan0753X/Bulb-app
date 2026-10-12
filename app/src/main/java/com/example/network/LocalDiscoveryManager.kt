package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import java.util.concurrent.Executors

data class DiscoveredDevice(
    val serviceName: String,
    val serviceType: String,
    val hostAddress: String,
    val port: Int,
    val isLikelySmitch: Boolean = false,
    val httpProbeStatus: String? = null
)

class LocalDiscoveryManager(private val context: Context) {

    private val tag = "LocalDiscoveryManager"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _discoveryError = MutableStateFlow<String?>(null)
    val discoveryError: StateFlow<String?> = _discoveryError.asStateFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        const val SERVICE_TYPE = "_http._tcp."
    }

    fun startDiscovery() {
        if (_isDiscovering.value || nsdManager == null) return
        _discoveryError.value = null

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(tag, "Start discovery failed: $errorCode")
                _isDiscovering.value = false
                _discoveryError.value = "Discovery start failed (code: $errorCode)"
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(tag, "Stop discovery failed: $errorCode")
                _isDiscovering.value = false
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(tag, "Discovery started for $serviceType")
                _isDiscovering.value = true
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(tag, "Discovery stopped")
                _isDiscovering.value = false
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo == null) return
                Log.d(tag, "Service found: ${serviceInfo.serviceName}")
                resolveService(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo == null) return
                val current = _discoveredDevices.value.filterNot { it.serviceName == serviceInfo.serviceName }
                _discoveredDevices.value = current
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            _discoveryError.value = "Failed to start NSD: ${e.message}"
            _isDiscovering.value = false
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        if (nsdManager == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            nsdManager.registerServiceInfoCallback(
                serviceInfo,
                executor,
                object : NsdManager.ServiceInfoCallback {
                    override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                        Log.e(tag, "Service callback failed: $errorCode")
                    }

                    override fun onServiceUpdated(resolvedInfo: NsdServiceInfo) {
                        addResolvedDevice(resolvedInfo)
                    }

                    override fun onServiceLost() {}

                    override fun onServiceInfoCallbackUnregistered() {}
                }
            )
        } else {
            @Suppress("DEPRECATION")
            nsdManager.resolveService(
                serviceInfo,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo?, errorCode: Int) {
                        Log.e(tag, "Resolve failed: $errorCode for ${info?.serviceName}")
                    }

                    override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                        resolvedInfo?.let { addResolvedDevice(it) }
                    }
                }
            )
        }
    }

    private fun addResolvedDevice(resolvedInfo: NsdServiceInfo) {
        val host: InetAddress? = resolvedInfo.host
        val hostAddress = host?.hostAddress ?: return
        val port = resolvedInfo.port
        val serviceName = resolvedInfo.serviceName ?: "Unknown"

        val isLikelySmitch = serviceName.contains("smitch", ignoreCase = true) ||
                serviceName.contains("bulb", ignoreCase = true) ||
                serviceName.startsWith("Smitch_v1", ignoreCase = true)

        val newDevice = DiscoveredDevice(
            serviceName = serviceName,
            serviceType = resolvedInfo.serviceType ?: SERVICE_TYPE,
            hostAddress = hostAddress,
            port = if (port > 0) port else 80,
            isLikelySmitch = isLikelySmitch
        )

        val existing = _discoveredDevices.value.toMutableList()
        val index = existing.indexOfFirst { it.hostAddress == hostAddress && it.port == newDevice.port }
        if (index >= 0) {
            existing[index] = newDevice
        } else {
            existing.add(newDevice)
        }
        _discoveredDevices.value = existing
    }

    fun stopDiscovery() {
        if (!_isDiscovering.value || nsdManager == null) return
        try {
            discoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.e(tag, "Error stopping discovery", e)
        } finally {
            _isDiscovering.value = false
            discoveryListener = null
        }
    }

    fun clearDevices() {
        _discoveredDevices.value = emptyList()
    }
}
