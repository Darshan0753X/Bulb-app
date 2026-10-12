package com.example.diagnostics

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiagnosticLogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
    val transport: String,
    val target: String,
    val commandBody: String,
    val framing: String,
    val writeSuccess: Boolean,
    val responseReceived: Boolean,
    val rawResponse: String? = null,
    val parseSuccess: Boolean? = null,
    val errorMessage: String? = null
)

object DiagnosticLogger {
    private const val MAX_LOGS = 100
    private val _logs = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
    val logs: StateFlow<List<DiagnosticLogEntry>> = _logs.asStateFlow()

    private val _lastEntry = MutableStateFlow<DiagnosticLogEntry?>(null)
    val lastEntry: StateFlow<DiagnosticLogEntry?> = _lastEntry.asStateFlow()

    fun log(entry: DiagnosticLogEntry) {
        _lastEntry.value = entry
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > MAX_LOGS) {
            _logs.value = current.take(MAX_LOGS)
        } else {
            _logs.value = current
        }
    }

    fun clear() {
        _logs.value = emptyList()
        _lastEntry.value = null
    }
}
