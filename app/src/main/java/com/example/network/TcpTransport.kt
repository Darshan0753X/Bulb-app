package com.example.network

import com.example.diagnostics.DiagnosticLogEntry
import com.example.diagnostics.DiagnosticLogger
import com.example.protocol.FramingStrategy
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TransportResult(
    val success: Boolean,
    val commandBody: String,
    val framedLength: Int,
    val response: String?,
    val error: String? = null
)

interface BulbTransport {
    val name: String
    suspend fun sendCommand(
        host: String,
        port: Int,
        commandBody: String,
        framing: FramingStrategy,
        readResponse: Boolean = true,
        connectTimeoutMs: Int = 3000,
        readTimeoutMs: Int = 2500,
        maxResponseBytes: Int = 4096
    ): TransportResult
}

/**
 * Robust TCP transport handling bounded connection, write, and read.
 * Closes sockets safely. Does not run on UI thread.
 */
class TcpTransport(override val name: String) : BulbTransport {

    override suspend fun sendCommand(
        host: String,
        port: Int,
        commandBody: String,
        framing: FramingStrategy,
        readResponse: Boolean,
        connectTimeoutMs: Int,
        readTimeoutMs: Int,
        maxResponseBytes: Int
    ): TransportResult = withContext(Dispatchers.IO) {
        val framedCommand = commandBody + framing.suffix
        val target = "$host:$port"
        var socket: Socket? = null
        var writeSuccess = false
        var responseReceived = false
        var responseText: String? = null
        var errorMessage: String? = null

        try {
            socket = Socket()
            socket.soTimeout = readTimeoutMs
            socket.connect(InetSocketAddress(host, port), connectTimeoutMs)

            val outStream: OutputStream = socket.getOutputStream()
            val bytes = framedCommand.toByteArray(Charsets.UTF_8)
            outStream.write(bytes)
            outStream.flush()
            writeSuccess = true

            if (readResponse) {
                val inStream: InputStream = socket.getInputStream()
                val buffer = ByteArray(maxResponseBytes)
                val bytesRead = inStream.read(buffer)
                if (bytesRead > 0) {
                    responseReceived = true
                    responseText = String(buffer, 0, bytesRead, Charsets.UTF_8).trim()
                }
            }

            DiagnosticLogger.log(
                DiagnosticLogEntry(
                    transport = name,
                    target = target,
                    commandBody = commandBody,
                    framing = framing.displayName,
                    writeSuccess = writeSuccess,
                    responseReceived = responseReceived,
                    rawResponse = responseText,
                    errorMessage = null
                )
            )

            TransportResult(
                success = writeSuccess,
                commandBody = commandBody,
                framedLength = bytes.size,
                response = responseText,
                error = null
            )
        } catch (e: Exception) {
            errorMessage = e.message ?: e.javaClass.simpleName
            DiagnosticLogger.log(
                DiagnosticLogEntry(
                    transport = name,
                    target = target,
                    commandBody = commandBody,
                    framing = framing.displayName,
                    writeSuccess = writeSuccess,
                    responseReceived = responseReceived,
                    rawResponse = responseText,
                    errorMessage = errorMessage
                )
            )
            TransportResult(
                success = false,
                commandBody = commandBody,
                framedLength = framedCommand.toByteArray(Charsets.UTF_8).size,
                response = responseText,
                error = errorMessage
            )
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }
}
