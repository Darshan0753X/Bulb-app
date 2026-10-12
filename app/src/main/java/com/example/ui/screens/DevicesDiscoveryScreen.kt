package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.DiscoveredDevice
import com.example.repository.ConnectionMode
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.viewmodel.BulbViewModel

@Composable
fun DevicesDiscoveryScreen(
    viewModel: BulbViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val discoveryError by viewModel.discoveryError.collectAsState()

    var customHost by remember(uiState.config.host) { mutableStateOf(uiState.config.host) }
    var customPort by remember(uiState.config.port) { mutableStateOf(uiState.config.port.toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(18.dp)
    ) {
        item {
            Text(
                text = "DEVICE NETWORKING",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Connect directly via Bulb Hotspot or discover over home Wi-Fi",
                fontSize = 12.sp,
                color = MutedGray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mode A vs Mode B Selection
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevatedDark.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE CONNECTION TARGET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode A: Hotspot
                    val isHotspot = uiState.config.mode == ConnectionMode.HOTSPOT_SETUP
                    Surface(
                        onClick = { viewModel.setConnectionMode(ConnectionMode.HOTSPOT_SETUP) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHotspot) NeonCyan.copy(alpha = 0.15f) else SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isHotspot) NeonCyan else BorderDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hotspot_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = if (isHotspot) NeonCyan else MutedGray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mode A: Bulb Hotspot",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isHotspot) Color.White else MutedGray
                                )
                                Text(
                                    text = "Connect phone to Smitch_v1ab • 192.168.4.1:80",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode B: Home Wi-Fi
                    val isLan = uiState.config.mode == ConnectionMode.HOME_LAN
                    Surface(
                        onClick = { viewModel.setConnectionMode(ConnectionMode.HOME_LAN) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLan) NeonCyan.copy(alpha = 0.15f) else SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLan) NeonCyan else BorderDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lan_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (isLan) NeonCyan else MutedGray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mode B: Home Wi-Fi LAN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLan) Color.White else MutedGray
                                )
                                Text(
                                    text = "Use mDNS NSD discovery or manual IP on router",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Manual Host & Port Entry
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevatedDark.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUAL HOST & PORT CONFIG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customHost,
                            onValueChange = { customHost = it },
                            label = { Text("IP Address", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("manual_ip_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = customPort,
                            onValueChange = { customPort = it },
                            label = { Text("Port", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_port_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ElevatedButton(
                        onClick = {
                            val p = customPort.toIntOrNull() ?: 80
                            viewModel.setCustomTarget(customHost.trim(), p)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_manual_target_button")
                    ) {
                        Text("Apply Target (${customHost}:${customPort})", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Local Discovery Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevatedDark.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL mDNS DISCOVERY (_http._tcp.)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isDiscovering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ElevatedButton(
                            onClick = {
                                if (isDiscovering) viewModel.stopDiscovery() else viewModel.startDiscovery()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (isDiscovering) NeonAmber else NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_discovery_button")
                        ) {
                            Icon(
                                imageVector = if (isDiscovering) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isDiscovering) "Stop Discovery" else "Start Scan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (discoveryError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = discoveryError ?: "",
                            fontSize = 11.sp,
                            color = NeonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Discovered Services (${discoveredDevices.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (discoveredDevices.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No devices discovered yet. Connect phone to same 2.4 GHz network or bulb hotspot and scan.",
                            fontSize = 12.sp,
                            color = MutedGray
                        )
                    }
                }
            }
        } else {
            items(discoveredDevices) { dev ->
                DiscoveredDeviceCard(
                    device = dev,
                    isSelected = uiState.config.host == dev.hostAddress && uiState.config.port == dev.port,
                    onSelect = { viewModel.selectDiscoveredDevice(dev) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DiscoveredDeviceCard(
    device: DiscoveredDevice,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) NeonCyan.copy(alpha = 0.12f) else SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else BorderDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovered_device_${device.hostAddress}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (device.isLikelySmitch) NeonGreen else MutedGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.serviceName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${device.hostAddress}:${device.port}  •  ${device.serviceType}",
                    fontSize = 11.sp,
                    color = MutedGray
                )
                if (device.isLikelySmitch) {
                    Text(
                        text = "Smitch signature detected",
                        fontSize = 10.sp,
                        color = NeonGreen
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onSelect,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isSelected) NeonCyan else Color.White
                )
            ) {
                Text(if (isSelected) "Selected" else "Select", fontSize = 11.sp)
            }
        }
    }
}
