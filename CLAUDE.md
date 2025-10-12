# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform project targeting Android, iOS, Web (WASM), and Server platforms. The project uses Compose Multiplatform for UI and Ktor for server-side development.

## Commands

### Build and Run
- **Web (Development)**: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` - Opens web application in browser
- **Server**: `./gradlew :server:run` - Runs Ktor server on port 8080
- **Android**: `./gradlew :composeApp:assembleDebug` - Build Android APK
- **iOS**: Open `iosApp/iosApp.xcodeproj` in Xcode to build and run

### Testing
- **All Tests**: `./gradlew test`
- **Common Tests**: `./gradlew :shared:test`
- **Server Tests**: `./gradlew :server:test`

### Build Tasks
- **Clean**: `./gradlew clean`
- **Build All**: `./gradlew build`

## Architecture

### Module Structure
- **`/shared`** (`/shared/src/commonMain/kotlin`): Core business logic shared across all platforms
- **`/composeApp`** (`/composeApp/src/commonMain/kotlin`): Compose Multiplatform UI code shared across platforms
  - Platform-specific code in `androidMain/`, `iosMain/`, `wasmJsMain/`, etc.
- **`/server`** (`/server/src/main/kotlin`): Ktor server application
- **`/iosApp`** (`/iosApp/iosApp`): iOS application entry point and SwiftUI code

### Directory Structure

#### composeApp (Feature-based Architecture)
```
composeApp/src/commonMain/kotlin/org/example/project/
├── core/                         # Shared components
│   └── di/                       # Dependency Injection configuration
├── feature/                      # Feature modules (one directory per feature)
│   ├── video_playback/           # Video playback feature
│   │   ├── ui/                   # UI components (Container, Screen, Content, etc.)
│   │   └── player/               # Player-specific logic (State, Controller, Templates)
│   ├── video_search/             # Video search feature
│   │   └── ui/                   # Search UI components
│   └── video_sync/               # Video synchronization feature
│       └── ui/                   # Sync UI components
└── (root level feature files)    # App.kt, platform views
```

**Architecture Pattern**: Each feature contains:
- `ui/`: Composable UI components
- `player/`: Player-specific logic (if applicable)
- ViewModel, Intent, UiState at feature root (MVI pattern)

#### shared (Clean Architecture - Domain & Data layers)
```
shared/src/commonMain/kotlin/org/example/project/
├── core/                         # Core utilities
│   ├── di/                       # Shared DI configuration
│   └── util/                     # Utility classes
├── domain/                       # Domain layer (business logic)
│   ├── model/                    # Domain models and entities
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Use cases (business logic)
├── data/                         # Data layer (implementation)
│   ├── datasource/               # Data sources (API clients, local storage)
│   ├── repository/               # Repository implementations
│   ├── mapper/                   # Data to domain mappers
│   └── model/                    # API response models
└── api/                          # External API clients
    └── (service-specific dirs)   # e.g., twitch/, youtube/
```

**Architecture Pattern**: Clean Architecture with clear layer separation
- Domain layer defines contracts (interfaces, models, use cases)
- Data layer implements contracts (repositories, data sources)
- Dependency rule: Data depends on Domain, never vice versa

### Key Files
- **Server configuration**: `shared/src/commonMain/kotlin/org/example/project/Constants.kt` - Contains `SERVER_PORT = 8080`
- **Main Compose UI**: `composeApp/src/commonMain/kotlin/org/example/project/App.kt`
- **Video playback**: `composeApp/src/commonMain/kotlin/org/example/project/feature/video_playback/`
- **Server routes**: `server/src/main/kotlin/org/example/project/Application.kt`

### Platform Targets
- **Android**: Uses Jetpack Compose with min SDK per gradle configuration
- **iOS**: Framework built as static library, consumed by SwiftUI app
- **Web**: Kotlin/WASM target with Compose for Web
- **Server**: JVM target using Ktor and Netty

The project follows standard Kotlin Multiplatform conventions with `commonMain` for shared code and platform-specific source sets for platform-specific implementations.

## Documentation Structure

### `/docs` Directory Organization
```
docs/
├── architecture/        # System architecture and design patterns documentation
├── adr/                 # Architecture Decision Records (ADR) - numbered decision logs
├── design-doc/          # Feature design documents with templates
└── context/             # GitHub issue context and task breakdown (one directory per issue)
```

### Documentation Guidelines

- **`architecture/`**: High-level system architecture, presentation patterns, component design guidelines
- **`adr/`**: Architecture Decision Records following ADR format (numbered: `NNN-kebab-case-title.md`)
- **`design-doc/`**: Detailed design specifications for features using provided templates
- **`context/`**: Issue-specific implementation context, including design references, interface definitions, and task breakdowns