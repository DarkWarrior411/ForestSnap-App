# ForestSnap

A comprehensive Android application for capturing, managing, and syncing forest survey snapshots with location data.

## Project Structure

```
ForestSnap/
├── build.gradle.kts              # Project-level Gradle configuration
├── settings.gradle.kts           # Gradle settings and module includes
├── .gitignore                    # Git ignore rules
│
└── app/                          # Main application module
    ├── build.gradle.kts          # App-level Gradle configuration
    ├── proguard-rules.pro        # ProGuard rules for code obfuscation
    │
    └── src/
        └── main/
            ├── AndroidManifest.xml        # App permissions and configuration
            │
            ├── res/
            │   ├── drawable/             # Custom drawables
            │   ├── mipmap/               # App launcher icons
            │   └── values/               # String, color, and dimension resources
            │                 ├── strings.xml
            │                 ├── colors.xml
            │                 └── themes.xml
            │
            └── java/com/example/forestsnap/
                │
                ├── MainActivity.kt                # App entry point
                ├── ForestSnapApplication.kt       # Custom Application class
                │
                ├── core/                         # Core functionality
                │   ├── theme/
                │   │   ├── Theme.kt
                │   │   └── Type.kt
                │   ├── navigation/
                │   │   └── NavGraph.kt
                │   └── utils/
                │       └── PreferenceManager.kt
                │
                ├── data/                         # Data layer
                │   ├── local/
                │   │   ├── ForestDatabase.kt
                │   │   ├── SyncSnapDao.kt
                │   │   └── SyncSnapEntity.kt
                │   └── repository/
                │       └── SyncSnapRepository.kt
                │
                └── features/                     # Feature modules
                    ├── dashboard/
                    │   ├── DashboardScreen.kt
                    │   ├── DashboardViewModel.kt
                    │   └── CameraScreen.kt
                    ├── map/
                    │   └── MapScreen.kt
                    ├── syncqueue/
                    │   ├── SyncQueueScreen.kt
                    │   └── SyncQueueViewModel.kt
                    └── settings/
                        └── SettingsScreen.kt
```

## Features

- **Dashboard**: Quick access to main app features
- **Camera**: Capture forest snapshots with GPS location data
- **Map**: Visualize snapshot locations on an interactive map
- **Sync Queue**: Manage pending uploads and retry failed syncs
- **Settings**: Configure app preferences and behavior

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room ORM
- **Async**: Coroutines & Flow
- **Navigation**: Jetpack Compose Navigation
- **Architecture**: MVVM with Repository pattern
- **Camera**: Camera X library
- **Maps**: Google Maps Compose

## Build & Run

### Prerequisites
- Android SDK 26+ (Min SDK)
- Android SDK 34 (Target SDK)
- Android Studio Arctic Fox or later

### Build Steps

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project:
   ```bash
   ./gradlew build
   ```

5. Run the app on an emulator or physical device:
   ```bash
   ./gradlew installDebug
   ```

## Development

The project follows modern Android development best practices:

- **Jetpack Compose** for UI
- **Room Database** for local data persistence
- **Coroutines** for async operations
- **Flow** for reactive streams
- **MVVM** architecture pattern
- **Repository Pattern** for data abstraction

## License

MIT License

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Support

For support, please create an issue in the repository.
