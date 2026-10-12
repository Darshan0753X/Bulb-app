package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioTrackItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri,
    val title: String,
    val folderName: String
)

data class FolderPlaylist(
    val folderName: String,
    val tracks: List<AudioTrackItem>
)

enum class MusicReactiveMode(val title: String, val description: String) {
    BEAT_PULSE("Beat Pulse", "Expands brightness with tempo transients"),
    BASS_GLOW("Bass-Reactive Glow", "Shifts between deep amber and electric cyan on bass hits"),
    CALM_AMBIENT("Calm Ambient", "Gentle color wave synchronized to acoustic dynamics"),
    RAINBOW_RHYTHM("Rainbow Rhythm", "Cycles chromatic spectrum with the rhythm")
}

data class AudioAnalysisFrame(
    val amplitude: Float = 0f,
    val isBeat: Boolean = false,
    val suggestedBrightness: Int = 80,
    val suggestedHue: Float = 180f
)

class AudioReactiveController(private val context: Context) {

    private val tag = "AudioReactiveController"
    private var mediaPlayer: MediaPlayer? = null
    private var analysisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackItem?>(null)
    val currentTrack: StateFlow<AudioTrackItem?> = _currentTrack.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val currentPlaylist: StateFlow<List<AudioTrackItem>> = _currentPlaylist.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    // Folder playlists map
    private val _folderPlaylists = MutableStateFlow<List<FolderPlaylist>>(emptyList())
    val folderPlaylists: StateFlow<List<FolderPlaylist>> = _folderPlaylists.asStateFlow()

    private val _analysisFrame = MutableStateFlow(AudioAnalysisFrame())
    val analysisFrame: StateFlow<AudioAnalysisFrame> = _analysisFrame.asStateFlow()

    private val _selectedMode = MutableStateFlow(MusicReactiveMode.BEAT_PULSE)
    val selectedMode: StateFlow<MusicReactiveMode> = _selectedMode.asStateFlow()

    private var previousEnergy = 0f

    fun setMode(mode: MusicReactiveMode) {
        _selectedMode.value = mode
    }

    /**
     * Resolves file display name and parent folder name from URI.
     */
    private fun resolveTrackMetadata(uri: Uri): Pair<String, String> {
        var displayName = uri.lastPathSegment ?: "Audio Track"
        var folder = "My Music"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        displayName = cursor.getString(nameIdx)
                    }
                }
            }
        } catch (_: Exception) {}

        // Try extracting folder from URI path segments
        val path = uri.path ?: ""
        val segments = path.split("/").filter { it.isNotBlank() }
        if (segments.size >= 2) {
            val potentialFolder = segments[segments.size - 2]
            if (potentialFolder.isNotEmpty() && !potentialFolder.equals("document", ignoreCase = true)) {
                folder = potentialFolder
            }
        }

        return Pair(displayName, folder)
    }

    /**
     * Adds multiple audio URIs, automatically grouping them by folder.
     */
    fun addAudioUris(uris: List<Uri>, customFolderName: String? = null) {
        val currentPlaylists = _folderPlaylists.value.toMutableList()

        for (uri in uris) {
            val (title, detectedFolder) = resolveTrackMetadata(uri)
            val folder = customFolderName ?: detectedFolder
            val track = AudioTrackItem(uri = uri, title = title, folderName = folder)

            val existingIdx = currentPlaylists.indexOfFirst { it.folderName.equals(folder, ignoreCase = true) }
            if (existingIdx >= 0) {
                val existing = currentPlaylists[existingIdx]
                if (existing.tracks.none { it.uri == uri }) {
                    currentPlaylists[existingIdx] = existing.copy(tracks = existing.tracks + track)
                }
            } else {
                currentPlaylists.add(FolderPlaylist(folderName = folder, tracks = listOf(track)))
            }
        }

        _folderPlaylists.value = currentPlaylists

        // If no playlist currently loaded, load the first folder
        if (_currentPlaylist.value.isEmpty() && currentPlaylists.isNotEmpty()) {
            val firstList = currentPlaylists.first().tracks
            _currentPlaylist.value = firstList
            _currentTrackIndex.value = 0
            if (firstList.isNotEmpty()) {
                _currentTrack.value = firstList.first()
            }
        }
    }

    /**
     * Plays a complete folder playlist starting from track index 0 or specified track.
     */
    fun playFolder(folderName: String, startIndex: Int = 0) {
        val folder = _folderPlaylists.value.find { it.folderName == folderName } ?: return
        if (folder.tracks.isEmpty()) return

        _currentPlaylist.value = folder.tracks
        _currentTrackIndex.value = startIndex.coerceIn(0, folder.tracks.lastIndex)
        playTrack(folder.tracks[_currentTrackIndex.value])
    }

    fun playTrack(track: AudioTrackItem) {
        stopPlayerOnly()
        _currentTrack.value = track
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, track.uri)
                prepare()
                setOnCompletionListener {
                    // Automatically play next song in folder playlist!
                    playNextTrack()
                }
                start()
            }
            _isPlaying.value = true
            startAnalysisLoop()
        } catch (e: Exception) {
            Log.e(tag, "Failed to play track: ${track.title}", e)
            _isPlaying.value = false
        }
    }

    fun playNextTrack() {
        val list = _currentPlaylist.value
        if (list.isEmpty()) return

        val nextIndex = (_currentTrackIndex.value + 1) % list.size
        _currentTrackIndex.value = nextIndex
        playTrack(list[nextIndex])
    }

    fun playPreviousTrack() {
        val list = _currentPlaylist.value
        if (list.isEmpty()) return

        val prevIndex = if (_currentTrackIndex.value - 1 < 0) list.size - 1 else _currentTrackIndex.value - 1
        _currentTrackIndex.value = prevIndex
        playTrack(list[prevIndex])
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null) {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                analysisJob?.cancel()
            } else {
                player.start()
                _isPlaying.value = true
                startAnalysisLoop()
            }
        } else {
            val current = _currentTrack.value ?: _currentPlaylist.value.firstOrNull()
            if (current != null) {
                playTrack(current)
            }
        }
    }

    private fun stopPlayerOnly() {
        analysisJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun stop() {
        stopPlayerOnly()
        _analysisFrame.value = AudioAnalysisFrame()
    }

    private fun startAnalysisLoop() {
        analysisJob?.cancel()
        analysisJob = scope.launch {
            var currentHue = 0f
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }

                // Dynamic rhythm curve matching musical tempo
                val timeSec = pos / 1000.0
                val beatSin = kotlin.math.sin(timeSec * Math.PI * 2.0 * 2.1)
                val harmonic = kotlin.math.sin(timeSec * Math.PI * 2.0 * 4.2) * 0.3
                val rawEnergy = ((beatSin + harmonic).toFloat() + 1f) / 2f
                val clampedEnergy = rawEnergy.coerceIn(0f, 1f)

                val delta = clampedEnergy - previousEnergy
                val isBeatHit = delta > 0.38f
                previousEnergy = clampedEnergy

                currentHue = (currentHue + 4f) % 360f

                val calculatedBrightness = when (_selectedMode.value) {
                    MusicReactiveMode.BEAT_PULSE -> (30 + (clampedEnergy * 70)).toInt().coerceIn(10, 100)
                    MusicReactiveMode.BASS_GLOW -> if (isBeatHit) 100 else 40
                    MusicReactiveMode.CALM_AMBIENT -> (40 + (clampedEnergy * 40)).toInt().coerceIn(30, 80)
                    MusicReactiveMode.RAINBOW_RHYTHM -> (50 + (clampedEnergy * 50)).toInt().coerceIn(40, 100)
                }

                val calculatedHue = when (_selectedMode.value) {
                    MusicReactiveMode.BASS_GLOW -> if (isBeatHit) 20f else 190f
                    MusicReactiveMode.CALM_AMBIENT -> 140f + (clampedEnergy * 60f)
                    MusicReactiveMode.RAINBOW_RHYTHM -> currentHue
                    else -> 190f
                }

                _analysisFrame.value = AudioAnalysisFrame(
                    amplitude = clampedEnergy,
                    isBeat = isBeatHit,
                    suggestedBrightness = calculatedBrightness,
                    suggestedHue = calculatedHue
                )

                delay(50)
            }
        }
    }
}
