# CollabStream Component 設計

## 概要

動画同期サービス CollabStream の UI Component 階層設計。

### 設計原則
- **Route → Screen → Content → Component** の4層構造
- **expect/actual** によるプラットフォーム抽象化

## Component階層

### 🛣️ Routes (Stateful)
- **責務**: Navigation管理、副作用実行、状態初期化
- **特徴**: 唯一のStatefulコンポーネント、LaunchEffect使用

```kotlin
@Composable
fun syncSessionRoute() {
    val state by viewModel.state.collectAsState()
    
    LaunchEffect(Unit) { viewModel.initialize() }
    
    SyncSessionScreen(state = state, onIntent = viewModel::send)
}
```

### 📱 Screens (Stateless)
- **責務**: 画面レイアウト構成、ContentとComponentの配置
- **特徴**: 状態とアクションの受け渡し

```kotlin
@Composable
fun syncSessionScreen(state: SyncSessionState, onIntent: (Intent) -> Unit) {
    Column {
        DualVideoPlayerContent(/* ... */)
        SyncControlContent(/* ... */)
    }
}
```

### 🧩 Contents (機能単位)
- **責務**: 特定機能のUI集約、複数Componentの組み合わせ

```kotlin
@Composable
fun dualVideoPlayerContent() {
    Row {
        VideoPlayerComponent(modifier = Modifier.weight(1f))
        VideoPlayerComponent(modifier = Modifier.weight(1f))
    }
}
```

### ⚙️ Components (再利用可能)
- **責務**: アトミックUI、プラットフォーム抽象化

```kotlin
@Composable
expect fun VideoPlayerComponent()

// Platform-specific implementations
actual fun VideoPlayerComponent() // Android
actual fun VideoPlayerComponent() // iOS  
actual fun VideoPlayerComponent() // Web
```


## ファイル構造

```
composeApp/src/commonMain/kotlin/ui/
├── routes/          # Stateful components
├── screens/         # Stateless layouts  
├── content/         # Feature groupings
└── components/      # Reusable UI + expect/actual
```

## まとめ

4層構造により明確な責任分離と高い再利用性を実現。expect/actualでマルチプラットフォーム対応。