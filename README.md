# Mastodon Widget for Android

A feature-rich Android widget and application that allows you to view live Mastodon feeds and post directly from your home screen.

## Features

- **Live Feed Display**: View real-time updates from your Mastodon home timeline
- **Post Composition**: Create new posts directly from the widget
- **Interactive Actions**: Favorite, reblog, and bookmark posts
- **Multi-Server Support**: Connect to any Mastodon instance
- **Local Caching**: Database caching for offline viewing
- **Background Sync**: Automatic feed updates via WorkManager
- **Material Design**: Modern UI built with Jetpack Compose

## Project Structure

```
MastodonWidgetAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/mastodon/widget/
│   │   │   ├── api/
│   │   │   │   ├── ApiClient.kt              # Retrofit client configuration
│   │   │   │   ├── MastodonApiService.kt     # API endpoints
│   │   │   │   └── model/
│   │   │   │       └── Models.kt             # Data models
│   │   │   ├── data/
│   │   │   │   ├── AppDatabase.kt            # Room database
│   │   │   │   ├── Converters.kt             # Type converters
│   │   │   │   ├── PreferenceManager.kt      # DataStore preferences
│   │   │   │   └── StatusDao.kt              # Database access
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.kt      # User account operations
│   │   │   │   └── StatusRepository.kt       # Feed and posting operations
│   │   │   ├── service/
│   │   │   │   ├── FeedSyncService.kt        # Background service
│   │   │   │   └── FeedUpdateService.kt      # WorkManager periodic sync
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt           # Main app entry point
│   │   │   │   ├── AuthActivity.kt           # Authentication screen
│   │   │   │   ├── PostActivity.kt           # Dedicated post activity
│   │   │   │   ├── component/
│   │   │   │   │   └── StatusCard.kt         # Reusable status display
│   │   │   │   ├── screen/
│   │   │   │   │   ├── FeedScreen.kt         # Feed display
│   │   │   │   │   └── PostScreen.kt         # Post composition
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Theme.kt              # Material theme
│   │   │   │   │   ├── Type.kt               # Typography
│   │   │   │   │   └── Shape.kt              # Shapes
│   │   │   │   └── viewmodel/
│   │   │   │       ├── FeedViewModel.kt      # Feed logic
│   │   │   │       └── PostViewModel.kt      # Posting logic
│   │   │   └── widget/
│   │   │       └── MastodonWidgetProvider.kt # Widget provider
│   │   ├── AndroidManifest.xml               # App configuration
│   │   └── res/
│   │       ├── layout/
│   │       │   └── widget_layout.xml         # Widget UI
│   │       ├── xml/
│   │       │   ├── mastodon_widget_info.xml  # Widget metadata
│   │       │   ├── backup_rules.xml
│   │       │   └── data_extraction_rules.xml
│   │       └── values/
│   │           ├── strings.xml
│   │           ├── colors.xml
│   │           └── styles.xml
│   └── build.gradle                          # App dependencies
├── build.gradle                              # Project configuration
└── settings.gradle                           # Module settings
```

## Dependencies

### Core Android
- `androidx.core-ktx` - Kotlin extensions
- `androidx.appcompat` - Backward compatibility
- `google.android.material` - Material Design components

### Jetpack Compose
- `androidx.compose.ui` - Compose UI
- `androidx.compose.material` - Material components
- `androidx.activity-activity-compose` - Activity support

### Networking & Serialization
- `retrofit2` - REST client
- `com.squareup.okhttp3` - HTTP client
- `gson` - JSON serialization

### Data Persistence
- `androidx.room` - Local database
- `androidx.datastore` - Preferences management

### Async
- `kotlinx-coroutines` - Async operations
- `androidx.lifecycle` - Lifecycle management
- `androidx.work` - Background scheduling

### Image Loading
- `coil-compose` - Image caching and loading

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Java 11 or higher
- Kotlin 1.9.20 or later

### 1. Clone or Download the Project
```bash
cd MastodonWidgetAndroid
```

### 2. Open in Android Studio
1. Open Android Studio
2. File → Open → Select the project directory
3. Wait for Gradle sync to complete

### 3. Build the Project
```bash
./gradlew build
```

### 4. Run on Emulator or Device
```bash
./gradlew installDebug
```

Alternatively, click "Run" in Android Studio.

## Configuration

### API Keys & OAuth
The app uses Mastodon OAuth for authentication. To enable login:

1. Register your app on your Mastodon instance:
   - Visit `https://<your-instance>/settings/applications`
   - Create a new application with redirect URI: `com.mastodon.widget://oauth`

2. Update `AuthActivity.kt` with your OAuth implementation

### Widget Configuration
Edit `mastodon_widget_info.xml` to customize:
- Minimum widget size
- Update frequency
- Resizable options

### Feed Sync Interval
Update the interval in `FeedUpdateService.kt`:
```kotlin
PeriodicWorkRequestBuilder<FeedUpdateService>(
    15,  // Change this value (in minutes)
    TimeUnit.MINUTES
)
```

## Usage

### Viewing Your Feed
1. Launch the app from your home screen
2. The feed will automatically load your home timeline
3. Scroll to see more posts
4. Pull-to-refresh to update the feed

### Creating Posts
1. Tap the floating "+" button
2. Type your post (max 500 characters)
3. Tap "Post" to publish
4. Posts are saved locally and synced with your server

### Interacting with Posts
- **Heart Icon**: Favorite a post
- **Repeat Icon**: Reblog (boost) a post
- **Comment Count**: View replies

### Using the Widget
1. Long-press your home screen
2. Select "Widgets"
3. Find "Mastodon Feed Widget"
4. Add to your home screen
5. The widget will show recent posts and offer quick post/refresh buttons

## API Integration

### Mastodon API Endpoints Implemented

#### Authentication
- `POST /apps` - Register application
- `POST /oauth/token` - Get access token

#### Accounts
- `GET /accounts/verify_credentials` - Current user info
- `GET /accounts/:id` - User profile

#### Timeline
- `GET /timelines/home` - Home feed
- `GET /timelines/public` - Public timeline
- `GET /timelines/tag/:hashtag` - Hashtag timeline
- `GET /statuses/:id/context` - Post context

#### Status Actions
- `POST /statuses` - Create post
- `POST /statuses/:id/favourite` - Favorite
- `POST /statuses/:id/reblog` - Reblog
- `POST /statuses/:id/bookmark` - Bookmark

## Architecture

### MVVM Pattern
- **Model**: Data classes and Room entities
- **View**: Jetpack Compose screens
- **ViewModel**: FeedViewModel, PostViewModel

### Repository Pattern
- `StatusRepository` - Feed and post operations
- `AccountRepository` - User account management

### Data Layer
- **Remote**: Retrofit + MastodonApiService
- **Local**: Room database + DataStore preferences

## Customization

### Theming
Edit `Theme.kt`, `Type.kt`, and `Shape.kt` to customize colors, typography, and shapes.

### Status Display
Modify `StatusCard.kt` to change how posts are displayed.

### Compose UI
Update screens in `ui/screen/` to change layouts.

## Troubleshooting

### "User not authenticated"
- Implement OAuth login in `AuthActivity.kt`
- Ensure token is properly stored in preferences

### Feed not updating
- Check internet connection
- Verify Mastodon server URL is correct
- Check WorkManager logs

### Widget not showing
- Ensure widget layout is properly configured
- Add widget to home screen
- Check widget provider registration in manifest

## Performance Optimization

- **Database Indexing**: Statuses are cached locally
- **Lazy Loading**: Feed uses pagination
- **Image Caching**: Coil handles image optimization
- **Background Sync**: WorkManager prevents excessive API calls

## Future Enhancements

- [ ] Real-time WebSocket updates
- [ ] Image upload and media attachments
- [ ] Search functionality
- [ ] User notifications
- [ ] Multiple account support
- [ ] Custom filters and muting
- [ ] Theme customization
- [ ] Accessibility improvements

## License

This project is provided as-is for personal and educational use.

## Contributing

Feel free to fork, modify, and improve this project!

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review Mastodon API documentation
3. Check Android documentation for Compose UI

---

**Last Updated**: April 2026
**Version**: 1.0.0
**Compatibility**: Android 8.0+ (API 26+)
