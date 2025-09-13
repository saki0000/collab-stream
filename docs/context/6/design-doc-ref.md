# Design Doc Reference - Issue #6

## Source
- **File**: `docs/design-doc/YouTube表示機能.md`
- **Created**: 2024-09-13
- **Status**: Complete with Interface Design

## Key Interface Definitions

### Core Interfaces

**VideoUiState (commonMain)**
```kotlin
data class VideoUiState(
    val videoId: String = "",
    val serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    val syncDateTime: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class VideoServiceType {
    YOUTUBE
}
```

**VideoPlayerView (expect/actual)**
```kotlin
// commonMain
expect fun VideoPlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
): @Composable Unit

// androidMain - YouTube Android Player API + AndroidView
actual fun VideoPlayerView(...)

// iosMain - WKWebView + iframe + UIKitView
actual fun VideoPlayerView(...)
```

## Implementation Strategy
- **Pattern**: expect/actual for platform-specific implementations
- **Android**: YouTube Android Player API integration
- **iOS**: WKWebView iframe embedding
- **Error Handling**: Unified error interface with Snackbar display

## Architecture Focus
- **Layer**: Presentation layer only (composeApp module)
- **No Backend**: No API or data storage required
- **UI State**: Simple state management with VideoUiState