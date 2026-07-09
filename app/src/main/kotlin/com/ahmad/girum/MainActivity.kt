package com.ahmad.girum

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmad.girum.ui.GirumApp
import com.ahmad.girum.ui.GirumViewModel

class MainActivity : ComponentActivity() {
    private val sharedText = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedText.value = extractSharedText(intent)
        val appContainer = (application as GirumApplication).appContainer

        setContent {
            val viewModel: GirumViewModel = viewModel(
                factory = GirumViewModel.factory(appContainer),
            )
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) {}
            val storagePermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) {}
            var handledSharedText by remember { mutableStateOf<String?>(null) }

            fun requestDownloadPermissions() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            LaunchedEffect(sharedText.value) {
                val text = sharedText.value
                if (!text.isNullOrBlank() && text != handledSharedText) {
                    handledSharedText = text
                    requestDownloadPermissions()
                    viewModel.analyze(text)
                }
            }

            GirumApp(
                viewModel = viewModel,
                onBeforeDownload = ::requestDownloadPermissions,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText.value = extractSharedText(intent)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }
}
