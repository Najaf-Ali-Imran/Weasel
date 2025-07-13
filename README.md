#  Weasel 

<div align="center">
  <img src="https://res.cloudinary.com/dniybrzfx/image/upload/v1752334708/ChatGPT_Image_Jul_12_2025_08_36_29_PM-fotor-2025071220386_yxxsmk.png" alt="Weasel Music Logo" width="150" height="150">
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
  [![Version](https://img.shields.io/badge/Version-0.1.1--beta-orange.svg)](https://github.com/Najaf-Ali-Imran/weasel/releases)
</div>

<p align="center">
  <strong>A modern, open-source music streaming application for Android</strong>
</p>

<p align="center">
  Built with Kotlin and Jetpack Compose, Weasel leverages the NewPipe Extractor to provide a seamless listening experience without subscriptions. Discover, stream, and download your favorite music with a clean, intuitive interface designed for performance and user experience.
</p>

<div align="center">
  <img src="https://res.cloudinary.com/dniybrzfx/image/upload/v1752332256/WhatsApp_Image_2025-07-12_at_7.53.55_PM_odrnlx.jpg" alt="Weasel Home Screen" width="250">
  <img src="https://res.cloudinary.com/dniybrzfx/image/upload/v1752332256/WhatsApp_Image_2025-07-12_at_7.53.56_PM_jbzann.jpg" alt="Weasel Player Screen" width="250">
</div>

---

## ✨ Features

### 🎧 Core Playback & Streaming
- **🔍 Instant Search**: Lightning-fast search across tracks, artists, and playlists
- **🎵 High-Quality Streaming**: Automatically fetches the best available audio streams
- **🔄 Background Playback**: Seamless background audio with rich media notifications
- **🎚️ Endless Queue**: Smart auto-queuing of related tracks for continuous discovery

### 📚 Library Management
- **💾 Local Music Integration**: Automatic scanning and integration of device audio files
- **📝 Playlist Creation**: Create and manage custom playlists effortlessly
- **➕ Easy Track Addition**: Add any track—streamed or local—to your playlists
- **🕐 Listening History**: Comprehensive playback history accessible from Home and Settings

### 📱 Offline Experience
- **⬇️ Download Functionality**: Download any track for offline enjoyment
- **🔧 Robust Download Manager**: Background downloads with progress tracking via WorkManager
- **🧠 Smart Offline Playback**: Automatic local file playback to save mobile data
- **📵 Offline UI Adaptation**: Intelligent UI that adapts when offline, showing available content

### 🎨 Modern UI & UX
- **🏗️ Jetpack Compose**: Fully declarative, modern UI toolkit for responsive design
- **🎭 Themed Interface**: Clean, visually appealing design with consistent theming
- **✨ Polished Animations**: Custom zoom animations and smooth transitions

---

## 🏗️ Architecture

Weasel follows a scalable, maintainable architecture pattern with clear separation of concerns:

```
📦 weasel/
├── 📁 data/                    # Data layer
│   ├── 📁 local/              # Room Database components
│   │   ├── AppDatabase.kt
│   │   ├── MusicDao.kt
│   │   └── entities/
│   |                          # Network data sources
│   ├── Track.kt               # Data models
│   ├── Playlist.kt
│   └── DownloadWorker.kt      # Background download logic
├── 📁 di/                     # Dependency Injection
│   └── ViewModelFactory.kt
├── 📁 player/                 # Media3 playback components
│   └── MusicPlayerService.kt
├── 📁 repository/             # Data repositories
│   ├── LocalMusicRepository.kt
│   └── NewPipeMusicRepository.kt
├── 📁 ui/                     # UI resources and themes
│   ├── 📁 theme/
│   └── 📁 resources/
├── 📁 util/                   # Utility classes
│   └── AppConnectivityManager.kt
├── 📁 ux/                     # Jetpack Compose screens
│   ├── HomeScreen.kt
│   ├── NowPlayingScreen.kt
│   ├── PlaylistScreen.kt
│   └── components/
└── 📁 viewmodel/              # ViewModels
    ├── HomeViewModel.kt
    ├── MusicPlayerViewModel.kt
    └── PlaylistViewModel.kt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable version)
- Android SDK 24+ (API level 24 and above)
- Kotlin 2.0.0

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Najaf-Ali-Imran/weasel.git
   cd weasel
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Sync and Build**
   - Let Gradle sync and download dependencies
   - Build the project (`Build > Make Project`)

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift + F10`

---

## 🛠️ Tech Stack

<div align="center">

| Category | Technology |
|----------|------------|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) |
| **UI Framework** | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white) |
| **Architecture** | ![MVVM](https://img.shields.io/badge/MVVM-FF6B6B?style=for-the-badge) |
| **Database** | ![Room](https://img.shields.io/badge/Room-4285F4?style=for-the-badge&logo=android&logoColor=white) |
| **Media Playback** | ![ExoPlayer](https://img.shields.io/badge/ExoPlayer-FF0000?style=for-the-badge&logo=youtube&logoColor=white) |
| **Networking** | ![NewPipe](https://img.shields.io/badge/NewPipe%20Extractor-FF0000?style=for-the-badge) |
| **Background Tasks** | ![WorkManager](https://img.shields.io/badge/WorkManager-4285F4?style=for-the-badge&logo=android&logoColor=white) |
| **Concurrency** | ![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) |

</div>

---

## 📋 Releases

### 🎯 v0.1.1-beta - *Initial Beta Release*
**Released**: 7-12-2025

**What's New:**
- ✅ Complete core functionality implementation
- ✅ All features listed above fully functional
- ✅ Stable foundation for user testing
- 🎯 Focus on gathering user feedback and bug reports

**Download:** [v0.1.0](https://github.com/Najaf-Ali-Imran/Weasel/releases/tag/v0.1.0)

---


### 📮 Issues

Found a bug or have a feature request?  
👉 [Report it here](https://github.com/Najaf-Ali-Imran/weasel/issues)


### 👩‍💻 Code Contributions

**Step 1: Fork & Clone**
```bash
git fork https://github.com/Najaf-Ali-Imran/weasel.git
git clone https://github.com/Najaf-Ali-Imran/weasel.git
cd weasel
```

**Step 2: Create a Branch**
```bash
git checkout -b feature/amazing-new-feature
# or
git checkout -b fix/critical-bug-fix
```

**Step 3: Make Your Changes**
- Follow the existing code style and architecture
- Write meaningful commit messages
- Test your changes thoroughly
- Add documentation if needed

**Step 4: Submit a Pull Request**
- Push your branch to your fork
- Open a Pull Request against the `main` branch
- Provide a clear title and detailed description
- Link any relevant issues

### 📝 Code Style Guidelines

- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Write unit tests for new features

---

## 🔧 Development Notes

### Key Technical Decisions

**Offline-First Architecture**: The app prioritizes local downloaded files over streaming when available, ensuring optimal performance and data usage.

**Endless Queue Implementation**: Uses `onMediaItemTransition` events to dynamically fetch and append related tracks, creating a seamless listening experience.

**Privacy-Focused**: All user data (playlists, history) is stored locally. No personal information is transmitted to external servers.

### Performance Optimizations

- **Lazy Loading**: UI components load content on-demand
- **Efficient Caching**: Smart caching strategies for images and metadata
- **Background Processing**: Heavy operations run on background threads
- **Memory Management**: Proper lifecycle handling to prevent memory leaks

---

## 📄 License

This project is licensed under the License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **[NewPipe Team](https://github.com/TeamNewPipe)** 
- **[ExoPlayer Team](https://github.com/google/ExoPlayer)** 

---

<div align="center">
  
  **⭐ If you like Weasel, please give it a star! ⭐**
  
  <p>
    <a href="https://github.com/Najaf-Ali-Imran/weasel/stargazers">
      <img src="https://img.shields.io/github/stars/Najaf-Ali-Imran/weasel?style=social" alt="GitHub stars">
    </a>
    <a href="https://github.com/Najaf-Ali-Imran/weasel/network/members">
      <img src="https://img.shields.io/github/forks/Najaf-Ali-Imran/weasel?style=social" alt="GitHub forks">
    </a>
    <a href="https://github.com/Najaf-Ali-Imran/weasel/issues">
      <img src="https://img.shields.io/github/issues/Najaf-Ali-Imran/weasel" alt="GitHub issues">
    </a>
  </p>
  
  
</div>
