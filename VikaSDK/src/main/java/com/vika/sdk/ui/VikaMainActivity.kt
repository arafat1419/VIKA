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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaLanguage
import com.vika.sdk.models.VikaUIOptions
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
                    imageVector = Icons.Default.Mic,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = VikaTheme.colors

    // ViewModel for chat management
    val viewModel: VikaViewModel = viewModel(
        factory = VikaViewModelFactory(context)
    )
    val chatMessages by viewModel.chatMessages.collectAsState()
    val listState = rememberLazyListState()

    // Get session ID from SDK
    val sessionId = try {
        VikaSDK.getInstance().getSessionId()
    } catch (_: Exception) {
        null
    }

    // Load chat messages for current session
    LaunchedEffect(sessionId) {
        sessionId?.let { viewModel.loadMessages(it) }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

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
    var downloadedAudioFile by remember { mutableStateOf<java.io.File?>(null) }

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
            listeningText = "" // Don't show "Listening" text, just show WaveForm
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

        // Delete downloaded audio file if it exists
        downloadedAudioFile?.let { file ->
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            downloadedAudioFile = null
        }

        // Clear status text after playback
        listeningText = ""

        // Navigate if there's a pending navigation
        val deepLink = pendingNavigation
        val screenName = pendingScreenName

        if (deepLink != null) {
            // Show "Navigating to..." message briefly
            listeningText = if (screenName != null) {
                VikaStrings.navigatingTo(language, screenName)
            } else {
                VikaStrings.navigating(language)
            }

            // Navigate after delay
            scope.launch {
                delay(1500) // 1.5 second delay
                pendingNavigation = null
                pendingReplyText = null
                pendingScreenName = null
                listeningText = ""
                onNavigate(deepLink)
            }
        } else {
            // Clear pending data
            pendingReplyText = null
            pendingScreenName = null
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
                listeningText = "" // Don't show "Speaking" text, just show WaveForm
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

                    // Set up listener for socket response before sending
                    VikaSDK.getInstance()
                        .setConversationListener(object : VikaSDK.ConversationListener {
                            override fun onTranscriptionCompleted(event: com.vika.sdk.network.models.TranscriptionCompletedEvent) {
                                // Add user message and placeholder AI message to chat
                                sessionId?.let { sid ->
                                    viewModel.addUserMessage(
                                        sessionId = sid,
                                        conversationId = event.conversationId,
                                        userTranscription = event.transcription
                                    )
                                }
                                // Clear status text - transcription is now in chat
                                listeningText = ""
                            }

                            override fun onConversationProcessed(event: com.vika.sdk.network.models.ConversationProcessedEvent) {
                                // Update placeholder AI message with actual reply
                                sessionId?.let { sid ->
                                    viewModel.updateAIMessage(
                                        sessionId = sid,
                                        conversationId = event.conversationId,
                                        aiReply = event.result.replyText
                                    )
                                }

                                // Store reply text and navigation for after playback
                                pendingReplyText = event.result.replyText
                                event.result.navigation?.let { nav ->
                                    pendingNavigation = nav.deepLink
                                    pendingScreenName = nav.screenName
                                }

                                // Play back the reply audio if available
                                val replyAudioUrl = event.result.replyAudioUrl
                                if (replyAudioUrl != null) {
                                    scope.launch {
                                        try {
                                            listeningText = VikaStrings.processing(language)
                                            val cacheDir = context.externalCacheDir
                                            val downloadedFile =
                                                com.vika.sdk.utils.AudioHelper.downloadAudio(
                                                    audioUrl = replyAudioUrl,
                                                    outputDir = cacheDir!!,
                                                    fileName = "reply_audio.wav"
                                                )
                                            downloadedAudioFile = downloadedFile
                                            play(downloadedFile.absolutePath)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Clear status on error - don't play anything
                                            listeningText = ""
                                        }
                                    }
                                }
                                // Don't play recorded audio - reply is already in chat
                            }

                            override fun onError(error: com.vika.sdk.models.VikaError) {
                                listeningText = VikaStrings.error(language, error.message)
                                // Don't play recording on error - just show error message
                            }
                        })

                    // Send conversation - result comes via socket
                    VikaSDK.getInstance().sendConversation(
                        audioFile = audioFile,
                        callback = object : VikaSDK.ConversationCallback {
                            override fun onStarted() {
                                listeningText = VikaStrings.sending(language)
                            }

                            override fun onSuccess(response: com.vika.sdk.network.models.ConversationResponse) {
                                // Audio submitted, waiting for socket result
                                listeningText = VikaStrings.processing(language)
                            }

                            override fun onError(error: Throwable) {
                                listeningText =
                                    VikaStrings.error(language, error.message ?: "Unknown error")
                                // Don't play recording on error - just show error message
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    listeningText = VikaStrings.error(language, "Failed to send recording")
                    // Don't play recording on exception - just show error message
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
            .background(colors.background)
            .padding(horizontal = padding, vertical = verticalPadding)
    ) {
        // Top row with app branding and info icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App logo and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appLogoResId?.let { logoRes ->
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

            // Info icon
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

        Spacer(modifier = Modifier.height(16.dp))

        // Middle: Chat list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom: Buttons row with waveform/text in between
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Record/Stop button (left)
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
                        imageVector = if (isRecording) {
                            Icons.Default.Stop
                        } else {
                            Icons.Default.Mic
                        },
                        contentDescription = if (isRecording) "Stop" else "Record",
                        tint = colors.background,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Middle: WaveForm OR Status Text (mutually exclusive)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .height(if (isCompact) 60.dp else 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (listeningText.isNotEmpty()) {
                    // Status text (processing, sending, error, etc.)
                    Text(
                        text = listeningText,
                        color = colors.text,
                        fontSize = if (isCompact) 14.sp else 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Waveform view (always show when no status text)
                    WaveformView(
                        amplitude = amplitude,
                        isActive = isRecording || isPlaying,
                        color = colors.waveform,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 60.dp else 80.dp)
                    )
                }
            }

            // Close button (right)
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