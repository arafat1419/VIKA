package com.vika.sdk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaUIOptions
import com.vika.sdk.ui.ui.theme.VikaCustomTheme
import com.vika.sdk.ui.ui.theme.VikaTheme
import kotlinx.coroutines.launch

/**
 * Activity that displays the VIKA SDK UI as a bottom sheet.
 */
internal class VikaBottomSheetActivity : ComponentActivity() {
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

        // Make activity transparent
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.6f)

        val uiOptions = VikaSDK.currentUIOptions ?: VikaUIOptions()

        setContent {
            VikaCustomTheme(themeConfig = uiOptions.themeConfig) {
                VikaBottomSheetContent(
                    fileName = "${externalCacheDir?.absolutePath}/outcoming_audio.mp3",
                    appLogoResId = uiOptions.appLogoResId,
                    appTitle = uiOptions.appTitle,
                    dismissOnTouchOutside = uiOptions.dismissOnTouchOutside,
                    onDismiss = { finish() },
                    onStopRecording = { },
                    onRequestPermission = { requestAudioPermission() },
                    onNavigate = { deepLink ->
                        VikaSDK.getInstance().executeDeepLinkNavigation(deepLink)
                        finish()
                    }
                )
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
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VikaBottomSheetContent(
    fileName: String,
    appLogoResId: Int?,
    appTitle: String?,
    dismissOnTouchOutside: Boolean,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit,
    onRequestPermission: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val colors = VikaTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            if (dismissOnTouchOutside) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = colors.background,
        dragHandle = null
    ) {
        AudioRecordScreen(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(8.dp),
            fileName = fileName,
            appLogoResId = appLogoResId,
            appTitle = appTitle,
            isCompact = true,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
            onStopRecording = onStopRecording,
            onRequestPermission = onRequestPermission,
            onNavigate = onNavigate
        )
    }
}
