package com.turkcell.rencarapp.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Tam çözünürlüklü JPEG fotoğraf çekimi (FileProvider + TakePicture).
 */
@Composable
fun rememberJpegPhotoCaptureLauncher(
    onPhotoCaptured: (ByteArray) -> Unit,
    onCancelled: () -> Unit = {},
): () -> Unit {
    val context = LocalContext.current
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val captureFile = pendingCaptureFile
        pendingCaptureFile = null
        if (success && captureFile != null && captureFile.exists()) {
            val bytes = captureFile.readBytes()
            captureFile.delete()
            if (bytes.isNotEmpty()) {
                onPhotoCaptured(bytes)
            } else {
                onCancelled()
            }
        } else {
            captureFile?.delete()
            onCancelled()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchJpegCapture(context, takePictureLauncher) { file ->
                pendingCaptureFile = file
            }
        } else {
            Toast.makeText(context, "Kamera izni gerekli.", Toast.LENGTH_LONG).show()
            onCancelled()
        }
    }

    return {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> {
                launchJpegCapture(context, takePictureLauncher) { file ->
                    pendingCaptureFile = file
                }
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun launchJpegCapture(
    context: Context,
    launcher: ActivityResultLauncher<Uri>,
    onFileReady: (File) -> Unit,
) {
    val captureFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    onFileReady(captureFile)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        captureFile,
    )
    launcher.launch(uri)
}
