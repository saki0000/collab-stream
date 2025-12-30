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

## Development Workflow

CollabStreamプロジェクトでは、AI（Claude Code）を活用した仕様駆動開発（SDD）ワークフローを採用しています。

### Workflow Overview

開発は以下の4つのPhaseで構成されます：

- **Phase 0**: Epic定義 & 共通盤の切り出し（大規模機能のみ）
- **Phase 1**: 仕様・インターフェース定義（合意レビュー）
- **Phase 2**: AIによる実装
- **Phase 3**: 実装レビュー

### Key Documents

詳細なワークフローガイドラインは以下を参照してください：

- **Main Guide**: `docs/guides/development-workflow.md` - 全体フローの詳細
- **Templates**: `docs/design-doc/template/` - Epic/REQUIREMENTS/Design Docテンプレート

### Quick Start

1. **新機能開発開始時**:
   - 3 Story以上の大規模機能 → Phase 0から開始（Epic作成）
   - 小規模機能 → Phase 1から開始（REQUIREMENTS.md作成）

2. **Phase 1（仕様定義）**:
   - `feature/{feature_name}/REQUIREMENTS.md`を作成
   - インターフェースファースト設計
   - レビュー合意後にGitHub Issue作成

3. **Phase 2（AI実装）**:
   - REQUIREMENTSに基づきAIが実装
   - Serena Skillの活用推奨

4. **Phase 3（レビュー）**:
   - 仕様適合性とADR準拠を確認
   - PR作成と実装記録（`docs/context/{issue}/`）

### Testing Conventions

- **Framework**: kotlin.test (NOT JUnit 5 directly)
- **Style**: ネストクラス + バッククォート関数名
- **Location**:
  - `shared/src/commonTest/`: 共通テスト
  - `shared/src/jvmTest/`: JVM固有テスト
  - `composeApp/src/commonTest/`: UI層テスト

```kotlin
class VideoSyncUseCaseTest {
    @Test
    fun `syncVideoToAbsoluteTime should calculate correct absolute time`() {
        // Arrange, Act, Assert
    }
}
```

For complete workflow details, see `docs/guides/development-workflow.md`.

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