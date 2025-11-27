package com.example.elgoharymusic

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel
import com.example.elgoharymusic.ui.theme.ElgoharyMusicTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import androidx.core.content.edit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val musicViewModel: MusicViewModel by viewModels()
    private val favViewModel: FavViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestWritePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>

    private var hasShownManageStorageDialog = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissionLaunchers()
        requestPermissions()

        setContent {
            val isDarkTheme by musicViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val locales = AppCompatDelegate.getApplicationLocales()
            val language = locales[0]?.language ?: Locale.getDefault().language

            ElgoharyMusicTheme(
                darkTheme = isDarkTheme,
                language = language
            ) {
                ModernMusicNavGraph(
                    musicViewModel,
                    favViewModel,
                    playlistViewModel,
                    language,
                    isDarkTheme,
                    this
                )
            }
        }
    }

    private fun setupPermissionLaunchers() {
        // Multiple permissions launcher for Android 13+ (Audio + Images)
        requestMultiplePermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false
            val imagesGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false

            if (audioGranted) {
                musicViewModel.loadSongs()

                // Request manage storage permission after audio is granted
                if (!hasShownManageStorageDialog) {
                    requestManageStoragePermission()
                }

                // Warn user if images permission not granted
                if (!imagesGranted) {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_album_art_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                showPermissionDeniedDialog(
                    title = getString(R.string.permission_storage_required),
                    message = getString(R.string.permission_storage_message),
                    isReadPermission = true
                )
            }
        }

        // Read Permission Launcher (for Android 10-12)
        requestReadPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                musicViewModel.loadSongs()
                // For Android 9 and below, request write permission after read is granted
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    val hasWrite =
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    if (!hasWrite) {
                        requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        musicViewModel.setStoragePermissionGranted(true)
                    }
                }
                // After read permission is granted, check for manage storage on Android 11+
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasShownManageStorageDialog) {
                    requestManageStoragePermission()
                }
            } else {
                showPermissionDeniedDialog(
                    title = getString(R.string.permission_storage_required),
                    message = getString(R.string.permission_storage_message),
                    isReadPermission = true
                )
            }
        }

        // Write Permission Launcher (for Android 9 and below)
        requestWritePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Add delay for Android 9 to ensure permission is fully processed
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    lifecycleScope.launch {
                        delay(500) // Wait for system to fully process permission
                        musicViewModel.setStoragePermissionGranted(true)
                    }
                } else {
                    musicViewModel.setStoragePermissionGranted(true)
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.toast_write_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Manage All Files Permission Launcher (for Android 11+)
        manageStoragePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val isGranted = Environment.isExternalStorageManager()
                musicViewModel.setStoragePermissionGranted(isGranted)

                if (isGranted) {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_storage_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_storage_denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun requestPermissions() {
        when {
            // Android 13+ (API 33+): READ_MEDIA_AUDIO + READ_MEDIA_IMAGES
            Build.VERSION.SDK_INT >= 33 -> {
                val hasAudioPermission =
                    checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                val hasImagesPermission =
                    checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

                if (!hasAudioPermission || !hasImagesPermission) {
                    // Check if we should show rationale for audio permission
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO)) {
                        showPermissionRationaleDialog(
                            title = getString(R.string.permission_media_access_required),
                            message = getString(R.string.permission_media_access_message),
                            permissions = arrayOf(
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        )
                    } else {
                        // Request both permissions at once
                        requestMultiplePermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        )
                    }
                } else {
                    musicViewModel.loadSongs()
                    // Request manage storage for editing capabilities
                    if (!hasShownManageStorageDialog) {
                        requestManageStoragePermission()
                    }
                }
            }

            // Android 11-12 (API 30-32): READ_EXTERNAL_STORAGE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val hasReadPermission =
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                if (!hasReadPermission) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showPermissionRationaleDialog(
                            title = getString(R.string.permission_storage_access_required),
                            message = getString(R.string.permission_storage_function_message),
                            permission = Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    } else {
                        requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    musicViewModel.loadSongs()
                    if (!hasShownManageStorageDialog) {
                        requestManageStoragePermission()
                    }
                }
            }

            // Android 10 (API 29): READ_EXTERNAL_STORAGE (Scoped Storage)
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                val hasReadPermission =
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                if (!hasReadPermission) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showPermissionRationaleDialog(
                            title = getString(R.string.permission_storage_access_required),
                            message = getString(R.string.permission_storage_function_message),
                            permission = Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    } else {
                        requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    musicViewModel.loadSongs()
                }
            }

            // Android 9 and below (API 28-): READ + WRITE_EXTERNAL_STORAGE
            else -> {
                val hasReadPermission =
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                val hasWritePermission =
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                when {
                    !hasReadPermission -> {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showPermissionRationaleDialog(
                                title = getString(R.string.permission_storage_access_required),
                                message = getString(R.string.permission_storage_function_message),
                                permission = Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } else {
                            requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    !hasWritePermission -> {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionRationaleDialog(
                                title = getString(R.string.permission_write_access_required),
                                message = getString(R.string.permission_write_message),
                                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        } else {
                            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }

                    else -> {
                        musicViewModel.loadSongs()
                        musicViewModel.setStoragePermissionGranted(true)
                    }
                }
            }
        }
    }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                hasShownManageStorageDialog = true
                showManageStorageDialog()
            } else {
                musicViewModel.setStoragePermissionGranted(true)
            }
        }
    }

    // Updated to support both single permission and multiple permissions
    private fun showPermissionRationaleDialog(
        title: String,
        message: String,
        permission: String? = null,
        permissions: Array<String>? = null
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_grant)) { dialog, _ ->
                dialog.dismiss()
                when {
                    permissions != null && Build.VERSION.SDK_INT >= 33 -> {
                        requestMultiplePermissionsLauncher.launch(permissions)
                    }
                    permission != null -> {
                        when (permission) {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_AUDIO -> {
                                requestReadPermissionLauncher.launch(permission)
                            }

                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                                requestWritePermissionLauncher.launch(permission)
                            }
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    getString(R.string.toast_permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showManageStorageDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_additional_optional))
                .setMessage(getString(R.string.permission_manage_storage_message))
                .setPositiveButton(getString(R.string.button_grant_permission)) { dialog, _ ->
                    dialog.dismiss()
                    launchManageStorageSettings()
                }
                .setNegativeButton(getString(R.string.button_skip)) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.toast_enable_editing_later),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setCancelable(true)
                .show()
        }
    }

    private fun showPermissionDeniedDialog(
        title: String,
        message: String,
        isReadPermission: Boolean
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_open_settings)) { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                dialog.dismiss()
                if (isReadPermission) {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun launchManageStorageSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:$packageName".toUri()
                manageStoragePermissionLauncher.launch(intent)
            } catch (e: Exception) {
                // Fallback to general settings
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStoragePermissionLauncher.launch(intent)
                } catch (ex: Exception) {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_unable_open_settings),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.toast_unable_open_settings_short),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions when returning from settings
        when {
            Build.VERSION.SDK_INT >= 33 -> {
                if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    musicViewModel.loadSongs()
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    musicViewModel.loadSongs()
                }
            }

            else -> {
                // Android 9 and below
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    musicViewModel.loadSongs()
                }
            }
        }

        // Update storage management permission status for edit/delete capabilities
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ requires MANAGE_EXTERNAL_STORAGE
                val hasPermission = Environment.isExternalStorageManager()
                musicViewModel.setStoragePermissionGranted(hasPermission)
            }

            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                // Android 10 - edit/delete handled by MediaStore APIs
                musicViewModel.setStoragePermissionGranted(true)
            }

            else -> {
                // Android 9 and below - requires WRITE_EXTERNAL_STORAGE
                val hasWrite =
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                musicViewModel.setStoragePermissionGranted(hasWrite)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hasShownManageStorageDialog = false
    }
}