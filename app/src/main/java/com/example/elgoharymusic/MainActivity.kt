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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import java.util.Locale
import androidx.core.content.edit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()
    private val favViewModel: FavViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestWritePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>

    private var hasShownManageStorageDialog = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissionLaunchers()
        requestPermissions()

        setContent {
            val isDarkTheme by musicViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val language by musicViewModel.language.collectAsStateWithLifecycle()

            // Observe language changes and recreate activity
            LaunchedEffect(language) {
                val currentLocale = resources.configuration.locales[0]
                val targetLocale = when (language) {
                    AppLanguage.ENGLISH -> Locale.ENGLISH
                    AppLanguage.ARABIC -> Locale("ar")
                }

                // Only recreate if locale actually changed
                if (currentLocale.language != targetLocale.language) {
                    updateLocale(targetLocale)
                    recreate()
                }
            }
                ElgoharyMusicTheme(darkTheme = isDarkTheme, language = language) {
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

    private fun updateLocale(locale: Locale) {
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
        }

        resources.updateConfiguration(config, resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val sharedPrefs = context.getSharedPreferences("app_preferences_temp", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language", "en") ?: "en"

        val locale = if (languageCode == "ar") Locale("ar") else Locale.ENGLISH
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    private fun setupPermissionLaunchers() {
        // Read Permission Launcher
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
                    title = "Storage Permission Required",
                    message = "This app needs storage permission to access and play your music files. Please grant the permission in app settings.",
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
                    "Write permission denied. Song editing will be disabled.",
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
                        "Full storage access granted. You can now edit and delete songs.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Storage management permission denied. Song editing and deletion will be limited.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun requestPermissions() {
        when {
            // Android 13+ (API 33+): READ_MEDIA_AUDIO
            Build.VERSION.SDK_INT >= 33 -> {
                val hasReadPermission =
                    checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED

                if (!hasReadPermission) {
                    // Check if we should show rationale
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO)) {
                        showPermissionRationaleDialog(
                            title = "Music Access Required",
                            message = "This app needs permission to access your music files to play them. Without this permission, the app cannot function.",
                            permission = Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else {
                        requestReadPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
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
                            title = "Storage Access Required",
                            message = "This app needs permission to access your music files to play them. Without this permission, the app cannot function.",
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
                            title = "Storage Access Required",
                            message = "This app needs permission to access your music files to play them. Without this permission, the app cannot function.",
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
                                title = "Storage Access Required",
                                message = "This app needs permission to access your music files to play them. Without this permission, the app cannot function.",
                                permission = Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } else {
                            requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    !hasWritePermission -> {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionRationaleDialog(
                                title = "Storage Write Access Required",
                                message = "This app needs write permission to edit song metadata and delete files.",
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

    private fun showPermissionRationaleDialog(title: String, message: String, permission: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
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
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Permission required to use the app",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showManageStorageDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AlertDialog.Builder(this)
                .setTitle("Additional Permission (Optional)")
                .setMessage(
                    "To enable editing and deleting songs, this app needs permission to manage files.\n\n" +
                            "This allows you to:\n" +
                            "• Edit song metadata (title, artist, album)\n" +
                            "• Delete songs from your device\n\n" +
                            "You can skip this if you only want to play music."
                )
                .setPositiveButton("Grant Permission") { dialog, _ ->
                    dialog.dismiss()
                    launchManageStorageSettings()
                }
                .setNegativeButton("Skip") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "You can enable editing later in app settings",
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
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
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
                        "Unable to open settings. Please enable manually.",
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
                "Unable to open settings",
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

fun Context.saveLanguagePreference(language: AppLanguage) {
    val sharedPrefs = getSharedPreferences("app_preferences_temp", Context.MODE_PRIVATE)
    sharedPrefs.edit {
        putString(
            "language",
            if (language == AppLanguage.ARABIC) "ar" else "en"
        )
    }
}