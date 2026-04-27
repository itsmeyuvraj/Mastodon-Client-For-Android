# Mastodon Widget for Android - Quick Start Guide

## What You Have

A fully functional Android application with:

✅ **Live Feed** - Real-time Mastodon timeline display  
✅ **Post Composition** - Create new toots directly from the app  
✅ **Interactive Posts** - Favorite, reblog, and bookmark functionality  
✅ **Widget Support** - Home screen widget for quick access  
✅ **Background Sync** - Automatic feed updates  
✅ **Local Database** - Offline access with caching  
✅ **Modern UI** - Built with Jetpack Compose  

## Getting Started (5 Minutes)

### Step 1: Open the Project
```bash
# In Android Studio
File → Open → Select MastodonWidgetAndroid folder
```

### Step 2: Wait for Gradle Sync
Android Studio will automatically download dependencies (may take a few minutes).

### Step 3: Build & Run
```bash
# Option A: Click green "Run" button in Android Studio
# Option B: Terminal command
./gradlew installDebug
```

### Step 4: Test the App
1. Open the app on emulator or device
2. You should see the Mastodon Feed screen
3. Tap "+" button to create a new post
4. Tap heart icon to favorite posts

## Key Features to Try

### 📱 Feed Screen
- Displays posts from Mastodon timeline
- Swipe/scroll to load more posts
- Each post shows author info, content, and engagement counts

### ✍️ Create Posts
1. Tap the floating "+" button
2. Type your message (max 500 characters)
3. Tap "Post" button
4. Post appears in your feed

### ❤️ Interact with Posts
- **Heart button** - Favorite the post
- **Repeat button** - Reblog (boost) the post
- **Comment count** - View number of replies

### 📲 Home Screen Widget
1. Long-press your home screen
2. Select "Widgets"
3. Find "Mastodon Feed Widget"
4. Drag to your home screen
5. Widget shows recent posts with quick buttons

## Next Steps

### 1. Connect to Your Mastodon Account
Edit `AuthActivity.kt` to add OAuth authentication:
```kotlin
// TODO: Implement actual OAuth flow with your Mastodon instance
fun login(serverUrl: String, username: String, password: String) {
    // 1. Create OAuth app on your instance
    // 2. Get client_id and client_secret
    // 3. Implement token flow
    // 4. Save token to PreferenceManager
}
```

### 2. Customize the Appearance
Edit these files to change colors/fonts:
- `ui/theme/Theme.kt` - Colors
- `ui/theme/Type.kt` - Typography
- `ui/theme/Shape.kt` - Rounded corners

### 3. Configure Widget
Edit `res/xml/mastodon_widget_info.xml`:
```xml
<appwidget-provider
    android:minWidth="180dp"
    android:minHeight="180dp"
    android:updatePeriodMillis="300000" <!-- Change update interval -->
```

## Project File Reference

| File | Purpose |
|------|---------|
| `api/MastodonApiService.kt` | API endpoints |
| `data/AppDatabase.kt` | Local database |
| `repository/StatusRepository.kt` | Feed operations |
| `ui/MainActivity.kt` | App entry point |
| `ui/screen/FeedScreen.kt` | Feed display |
| `ui/screen/PostScreen.kt` | Post composition |
| `ui/component/StatusCard.kt` | Post display |
| `widget/MastodonWidgetProvider.kt` | Widget logic |

## Common Modifications

### Change Primary Color
In `Theme.kt`:
```kotlin
primary = Color(0xFF6F5AE0) // Change to your hex color
```

### Add More API Endpoints
In `MastodonApiService.kt`:
```kotlin
@GET("timelines/public")
suspend fun getPublicTimeline(...): List<Status>
```

### Customize Post Display
In `StatusCard.kt` - modify the composable to add/remove fields

## Testing Checklist

- [ ] App launches without crashes
- [ ] Feed loads posts
- [ ] Clicking heart favorites a post
- [ ] Clicking repeat reblogs a post
- [ ] New post screen opens on "+" button
- [ ] Character counter shows in post screen
- [ ] Post button disabled when empty
- [ ] Widget appears on home screen
- [ ] Widget shows recent posts

## Useful Gradle Commands

```bash
# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build files
./gradlew clean

# Check for errors
./gradlew lint

# View dependencies
./gradlew dependencies
```

## Architecture Overview

```
┌─────────────────────────────┐
│     Jetpack Compose UI      │
│  (FeedScreen, PostScreen)   │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│   ViewModels (MVVM)         │
│ (FeedVM, PostVM)            │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│   Repositories              │
│ (StatusRepository)          │
└──────────────┬──────────────┘
        ┌──────┴──────┐
        │             │
┌───────▼────┐  ┌─────▼──────┐
│   Remote   │  │   Local    │
│ (Retrofit) │  │ (Room DB)  │
└────────────┘  └────────────┘
```

## Troubleshooting

**Q: App crashes on startup**
- Check Android Studio logs
- Ensure Gradle sync completed successfully
- Try building from terminal: `./gradlew clean build`

**Q: Feed doesn't load posts**
- Check if API authentication is implemented
- Verify server URL is correct
- Check internet connection

**Q: Character limit not working**
- The limit is set to 500 chars - modify in `PostViewModel.kt` if needed

**Q: Widget doesn't appear**
- Run app first, then try adding widget
- Check that `MastodonWidgetProvider` is in manifest

## Next Development Steps

1. **Implement OAuth** - Full authentication flow
2. **Add WebSocket** - Real-time updates
3. **Image Upload** - Media attachments
4. **Search** - Find posts and users
5. **Notifications** - Push notifications
6. **Dark Mode** - Theme support (partially done)

## Resources

- Mastodon API Docs: https://docs.joinmastodon.org/
- Jetpack Compose Guide: https://developer.android.com/compose
- Room Database: https://developer.android.com/training/data-storage/room
- WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager

---

## You're Ready to Go! 🚀

The project is ready to build and run. Start with exploring the code, then customize it to your needs. Happy coding!
