```{=html}
<p align="center">
```
` <img src="https://raw.githubusercontent.com/ahmedelgohary305/Elgohary-Music/master/resources/Elgohary-Music app icon.png" width="140"/>`{=html}
```{=html}
</p>
```
# **Elgohary Music**

**Elgohary Music** is a modern, smooth, and highly customizable offline
music player for Android, inspired by Samsung Music and built with
Jetpack Compose, Media3, and MVVM architecture.

------------------------------------------------------------------------

## ✨ Features

### 🎧 Music Playback

-   High-quality playback using Media3 + ExoPlayer
-   Background audio with notification & lock‑screen controls
-   Waveform progress bar visualization
-   Queue tools (add to queue, play next, remove, clear)
-   Drag & drop queue reordering

### 🗂 Library & Sorting

-   Browse Songs, Albums, Artists, Playlists
-   Real-time search
-   Sorting by:
    -   Song title (A--Z / Z--A)
    -   Artist name (A--Z / Z--A)
    -   Duration (short → long / long → short)
-   Favorites system
-   Fast loading using Room caching

### 🎛 Song Management

-   Edit audio metadata (title, artist, album)
-   Delete song from storage
-   Metadata editing using jaudiotagger
-   Auto refresh after edits

### 📋 Playlist System

-   Create, rename, delete playlists
-   Add/remove songs
-   Add playlist to queue
-   Playlist detail screen

### 🔧 Permissions Handling

Elgohary Music properly handles all required storage/audio permissions.

#### Android 13+

-   READ_MEDIA_AUDIO
-   READ_MEDIA_IMAGES
-   File changes through SAF

#### Android 10--12

-   READ_EXTERNAL_STORAGE
-   Writes via MediaStore

#### Android 9 and below

-   READ_EXTERNAL_STORAGE
-   WRITE_EXTERNAL_STORAGE

### 🌐 Localization & Themes

-   Full Arabic 🇪🇬 and English 🇬🇧 support
-   RTL support
-   Light, Dark, System theme modes
-   Persistent settings via DataStore

### 🎨 UI/UX

-   100% Jetpack Compose
-   Material 3 components
-   Smooth transitions & animations
-   Coil for album art

------------------------------------------------------------------------

## 🛠 Tech Stack

-   Kotlin\
-   Jetpack Compose\
-   Media3 / ExoPlayer\
-   Room Database\
-   DataStore Preferences\
-   Hilt (DI)\
-   jaudiotagger\
-   Coil\
-   Accompanist Permissions\
-   Reorderable lists library

------------------------------------------------------------------------

## 🚀 Getting Started

1.  Clone repo\
2.  Open in Android Studio\
3.  Sync Gradle\
4.  Run on device/emulator with audio files

------------------------------------------------------------------------

## 📌 Future Enhancements

-   Sleep timer\
-   Built‑in equalizer\
-   Folder-based browsing\
-   Lyrics display\
-   Cloud playlist backup

------------------------------------------------------------------------

## 📄 License

Add your preferred license here.
