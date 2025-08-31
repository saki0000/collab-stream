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

### Key Files
- **Server configuration**: `shared/src/commonMain/kotlin/org/example/project/Constants.kt` - Contains `SERVER_PORT = 8080`
- **Main Compose UI**: `composeApp/src/commonMain/kotlin/org/example/project/App.kt`
- **Server routes**: `server/src/main/kotlin/org/example/project/Application.kt`
- **Shared logic**: `shared/src/commonMain/kotlin/org/example/project/Greeting.kt`

### Platform Targets
- **Android**: Uses Jetpack Compose with min SDK per gradle configuration
- **iOS**: Framework built as static library, consumed by SwiftUI app
- **Web**: Kotlin/WASM target with Compose for Web
- **Server**: JVM target using Ktor and Netty

The project follows standard Kotlin Multiplatform conventions with `commonMain` for shared code and platform-specific source sets for platform-specific implementations.