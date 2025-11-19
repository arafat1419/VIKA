package com.vika.sdk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaLanguage
import com.vika.sdk.models.VikaUIOptions
import com.vika.sdk.network.models.RecordingData
import com.vika.sdk.ui.ui.theme.VikaCustomTheme
import com.vika.sdk.ui.ui.theme.VikaTheme
import com.vika.sdk.utils.WaveformView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

internal class VikaMainActivity : ComponentActivity() {
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Audio permission is required to record audio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uiOptions = VikaSDK.currentUIOptions ?: VikaUIOptions()

        setContent {
            VikaCustomTheme(themeConfig = uiOptions.themeConfig) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioRecordScreen(
                        modifier = Modifier.padding(innerPadding),
                        fileName = "${externalCacheDir?.absolutePath}/outcoming_audio.mp3",
                        appLogoResId = uiOptions.appLogoResId,
                        appTitle = uiOptions.appTitle,
                        onDismiss = {
                            finish()
                        },
                        onStopRecording = {
                        },
                        onRequestPermission = {
                            requestAudioPermission()
                        },
                        onNavigate = { deepLink ->
                            // Execute navigation and close the SDK UI
                            VikaSDK.getInstance().executeDeepLinkNavigation(deepLink)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun requestAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show rationale and request permission
                AlertDialog.Builder(this)
                    .setTitle("Audio Permission Required")
                    .setMessage("This app needs access to your microphone to record audio messages.")
                    .setPositiveButton("Grant") { _, _ ->
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            else -> {
                // Request permission directly
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

@Composable
internal fun AudioRecordScreen(
    modifier: Modifier = Modifier,
    fileName: String,
    appLogoResId: Int? = null,
    appTitle: String? = null,
    isCompact: Boolean = false,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit,
    onRequestPermission: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val colors = VikaTheme.colors

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Check permission when composable becomes active
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    if (hasPermission) {
        AudioRecordUI(
            fileName = fileName,
            appLogoResId = appLogoResId,
            appTitle = appTitle,
            isCompact = isCompact,
            onDismiss = onDismiss,
            onStopRecording = onStopRecording,
            onNavigate = onNavigate,
            modifier = modifier
        )
    } else {
        // Permission request UI
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = colors.text,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Microphone Access Required",
                    color = colors.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please grant microphone permission to record audio messages.",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onRequestPermission()
                        // Recheck permission after request
                        hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text(
                        text = "Grant Permission",
                        color = colors.background,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun AudioRecordUI(
    modifier: Modifier = Modifier,
    fileName: String,
    appLogoResId: Int? = null,
    appTitle: String? = null,
    isCompact: Boolean = false,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val colors = VikaTheme.colors

    // Get language from SDK config
    val language = try {
        VikaSDK.getInstance().getLanguage()
    } catch (_: Exception) {
        VikaLanguage.ENGLISH
    }

    // Adjust sizes for compact mode (dialog/bottom sheet)
    val padding = if (isCompact) 16.dp else 24.dp
    val verticalPadding = if (isCompact) 16.dp else 32.dp

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var listeningText by remember { mutableStateOf("") }
    var amplitude by remember { mutableIntStateOf(0) }

    // Store navigation data, reply text, and screen name to execute after playback
    var pendingNavigation by remember { mutableStateOf<String?>(null) }
    var pendingReplyText by remember { mutableStateOf<String?>(null) }
    var pendingScreenName by remember { mutableStateOf<String?>(null) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var visualizer by remember { mutableStateOf<Visualizer?>(null) }

    // Amplitude update loop for recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                mediaRecorder?.let {
                    amplitude = it.maxAmplitude
                }
                delay(100)
            }
        } else {
            amplitude = 0
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
            visualizer?.apply {
                enabled = false
                release()
            }
        }
    }

    fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(fileName)
                prepare()
                start()
            }
            isRecording = true
            listeningText = VikaStrings.listening(language)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun createVisualizer(audioSessionId: Int) {
        val rate = Visualizer.getMaxCaptureRate()
        visualizer = Visualizer(audioSessionId).apply {
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    waveform: ByteArray,
                    samplingRate: Int
                ) {
                    val intensity = (waveform[0].toFloat() + 128f) / 256
                    amplitude = (intensity * rate).toInt()
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    fft: ByteArray,
                    samplingRate: Int
                ) {
                }
            }, rate, true, false)
            enabled = true
        }
    }

    fun stopVisualizer() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        stopVisualizer()
        isPlaying = false
        amplitude = 0

        // Show reply text and navigate after delay
        val replyText = pendingReplyText
        val deepLink = pendingNavigation
        val screenName = pendingScreenName

        if (deepLink != null) {
            // Show "Navigating to..." message
            listeningText = if (screenName != null) {
                VikaStrings.navigatingTo(language, screenName)
            } else {
                VikaStrings.navigating(language)
            }

            // Navigate after delay
            scope.launch {
                delay(2500) // 2.5 second delay to read the navigation message
                pendingNavigation = null
                pendingReplyText = null
                pendingScreenName = null
                onNavigate(deepLink)
            }
        } else if (replyText != null) {
            // Show reply text if no navigation
            listeningText = replyText
            scope.launch {
                delay(2500)
                pendingReplyText = null
                listeningText = ""
            }
        } else {
            listeningText = ""
        }
    }

    fun play(filePath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            setOnPreparedListener {
                it.start()
                isPlaying = true
                listeningText = VikaStrings.speaking(language)
                createVisualizer(it.audioSessionId)
            }
            setOnCompletionListener {
                stopPlaying()
            }
            setOnErrorListener { _, _, _ ->
                isPlaying = false
                false
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            amplitude = 0
            listeningText = VikaStrings.processing(language)
            onStopRecording()

            // Send recording to API
            scope.launch {
                try {
                    val audioFile = java.io.File(fileName)
                    VikaSDK.getInstance().sendRecording(
                        audioFile = audioFile,
                        callback = object : VikaSDK.RecordingCallback {
                            override fun onStarted() {
                                listeningText = VikaStrings.sending(language)
                            }

                            override fun onSuccess(response: RecordingData) {
                                // Store reply text and navigation for after playback
                                pendingReplyText = response.replyText
                                response.navigation?.let { nav ->
                                    pendingNavigation = nav.deepLink
                                    // Look up screen name from registered screens
                                    val screen = VikaSDK.getInstance().getRegisteredScreens()
                                        .find { it.screenId == nav.screenId }
                                    pendingScreenName = screen?.screenName
                                }

                                // Play back the recorded audio (until BE provides reply audio)
                                // TODO: When BE is ready, download and play response.replyAudioUrl
                                play(fileName)
                            }

                            override fun onError(error: Throwable) {
                                listeningText =
                                    VikaStrings.error(language, error.message ?: "Unknown error")

                                // Still play back the recording on error
                                play(fileName)
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Play back the recording on exception
                    play(fileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = modifier
            .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
            .background(colors.background)
            .padding(horizontal = padding, vertical = verticalPadding)
    ) {
        // Top row with app branding and info icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App logo and title - top start
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appLogoResId?.let { logoRes ->
                    val context = LocalContext.current
                    val density = LocalDensity.current
                    val sizePx = with(density) { 32.dp.toPx().toInt() }
                    val drawable = ContextCompat.getDrawable(context, logoRes)
                    drawable?.let {
                        val bitmap = it.toBitmap(sizePx, sizePx)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                appTitle?.let { title ->
                    Text(
                        text = title,
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info icon - top end
            IconButton(
                onClick = { /* Handle info click */ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = colors.text
                )
            }
        }

        // Center content
        Column(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waveform view - square aspect ratio (smaller in compact mode)
            WaveformView(
                amplitude = amplitude,
                isActive = isRecording || isPlaying,
                color = colors.waveform,
                modifier = if (isCompact) {
                    Modifier.size(160.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                }
            )

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))

            // Listening text
            Text(
                text = listeningText,
                color = colors.text,
                fontSize = if (isCompact) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Record/Stop button - primary color background
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isRecording) {
                            stopRecording()
                        } else {
                            if (isPlaying) {
                                stopPlaying()
                            } else {
                                startRecording()
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording || isPlaying) {
                            Icons.Default.Stop
                        } else {
                            Icons.Default.Mic
                        },
                        contentDescription = if (isRecording || isPlaying) "Stop" else "Record",
                        tint = colors.background,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Close button - secondary color background
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

// Usage example:
@Preview
@Composable
fun AudioRecordUIExample() {
    AudioRecordUI(
        fileName = "/path/to/audio.m4a",
        onDismiss = { },
        onStopRecording = {
            // Handle recording stopped
        }
    )
}