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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaUIOptions
import com.vika.sdk.ui.ui.theme.VikaCustomTheme
import com.vika.sdk.ui.ui.theme.VikaTheme

/**
 * Activity that displays the VIKA SDK UI as a dialog overlay.
 */
internal class VikaDialogActivity : ComponentActivity() {
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

        // Make activity transparent to show dialog properly
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.6f)

        val uiOptions = VikaSDK.currentUIOptions ?: VikaUIOptions()

        // Handle dismissOnTouchOutside
        setFinishOnTouchOutside(uiOptions.dismissOnTouchOutside)

        setContent {
            VikaCustomTheme(themeConfig = uiOptions.themeConfig) {
                VikaDialogContent(
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

@Composable
private fun VikaDialogContent(
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

    Dialog(
        onDismissRequest = {
            if (dismissOnTouchOutside) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = dismissOnTouchOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colors.background
        ) {
            AudioRecordScreen(
                modifier = Modifier.padding(8.dp),
                fileName = fileName,
                appLogoResId = appLogoResId,
                appTitle = appTitle,
                isCompact = true,
                onDismiss = onDismiss,
                onStopRecording = onStopRecording,
                onRequestPermission = onRequestPermission,
                onNavigate = onNavigate
            )
        }
    }
}
