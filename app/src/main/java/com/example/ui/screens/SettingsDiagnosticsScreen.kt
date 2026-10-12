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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.DiagnosticLogEntry
import com.example.diagnostics.DiagnosticLogger
import com.example.protocol.BulbModel
import com.example.protocol.FramingStrategy
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDiagnosticsScreen(
    viewModel: BulbViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.diagnosticLogs.collectAsState()

    var testCommand by remember { mutableStateOf("0001xx03") }
    var readResponseToggle by remember { mutableStateOf(true) }

    var framingExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(18.dp)
    ) {
        item {
            Text(
                text = "SETTINGS & DIAGNOSTICS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Transport framing, protocol options, and safe socket trace",
                fontSize = 12.sp,
                color = MutedGray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Transport Configuration Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevatedDark.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FRAMING & PROTOCOL CONFIGURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Framing Strategy Dropdown
                    Text("TCP Terminator Framing", fontSize = 12.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = framingExpanded,
                        onExpandedChange = { framingExpanded = !framingExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.config.framing.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = framingExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = framingExpanded,
                            onDismissRequest = { framingExpanded = false }
                        ) {
                            FramingStrategy.values().forEach { strat ->
                                DropdownMenuItem(
                                    text = { Text(strat.displayName) },
                                    onClick = {
                                        viewModel.setFramingStrategy(strat)
                                        framingExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bulb Model Dropdown
                    Text("Bulb Model Specification", fontSize = 12.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = modelExpanded,
                        onExpandedChange = { modelExpanded = !modelExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.config.model.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false }
                        ) {
                            BulbModel.values().forEach { mdl ->
                                DropdownMenuItem(
                                    text = { Text(mdl.displayName) },
                                    onClick = {
                                        viewModel.setBulbModel(mdl)
                                        modelExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Experimental LAN Control Opt-In
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Experimental LAN Control", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Allows sending raw TCP lighting commands across router subnet", fontSize = 11.sp, color = MutedGray)
                        }
                        Switch(
                            checked = uiState.config.lanControlEnabled,
                            onCheckedChange = { viewModel.setLanControlEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = SurfaceDark)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Manual Command Diagnostic Sandbox
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevatedDark.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUAL COMMAND TESTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testCommand,
                        onValueChange = { testCommand = it },
                        label = { Text("Command Body (e.g. 0001xx03, 0001xx09xxx1)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagnostic_command_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Read Socket Response", fontSize = 12.sp, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = readResponseToggle,
                            onCheckedChange = { readResponseToggle = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = SurfaceDark)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ElevatedButton(
                        onClick = {
                            viewModel.sendDiagnosticCommand(
                                commandBody = testCommand.trim(),
                                framing = uiState.config.framing,
                                readResponse = readResponseToggle
                            )
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_diagnostic_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Command to ${uiState.config.host}:${uiState.config.port}", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Diagnostic Trace Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSMISSION LOGS (${logs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { DiagnosticLogger.clear() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Logs",
                        tint = MutedGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No transmission entries recorded yet", fontSize = 12.sp, color = MutedGray)
                    }
                }
            }
        } else {
            items(logs) { log ->
                DiagnosticLogCard(log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DiagnosticLogCard(log: DiagnosticLogEntry) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (log.writeSuccess) BorderDark else NeonRed.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = log.timestamp,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.target,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (log.writeSuccess) "WRITE OK" else "WRITE FAIL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (log.writeSuccess) NeonGreen else NeonRed
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CMD: ${log.commandBody} [${log.framing}]",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            if (log.rawResponse != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RESP: ${log.rawResponse}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonAmber
                )
            }

            if (log.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ERR: ${log.errorMessage}",
                    fontSize = 11.sp,
                    color = NeonRed
                )
            }
        }
    }
}
