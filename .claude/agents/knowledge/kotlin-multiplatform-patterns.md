# Kotlin Multiplatform Patterns & Knowledge Base

Kotlin Multiplatformプロジェクトでの実装パターン・ベストプラクティス集です。各Agentがこの知識を活用して一貫性のある実装を行います。

## 🏗️ アーキテクチャパターン

### 3層アーキテクチャ構成
```
┌─────────────────────────────────────┐
│           composeApp                │  ← UI Layer
│     (Compose Multiplatform)         │
├─────────────────────────────────────┤
│            shared                   │  ← Business Logic Layer  
│   (Common + Platform-specific)      │
├─────────────────────────────────────┤
│            server                   │  ← Server/API Layer
│        (Ktor Server)                │
└─────────────────────────────────────┘
```

### モジュール間依存関係
```kotlin
// composeApp が shared に依存
dependencies {
    implementation(projects.shared)
}

// server が shared に依存  
dependencies {
    implementation(projects.shared)
}

// shared は独立（commonMain + platform-specific)
```

## 📦 共通実装パターン

### 1. Entity/Data Class Pattern

**commonMain での定義:**
```kotlin
// shared/src/commonMain/kotlin/entity/User.kt
@kotlinx.serialization.Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Validation extension
fun User.isValid(): Boolean = 
    email.contains("@") && name.isNotBlank()
```

**プラットフォーム固有拡張:**
```kotlin
// shared/src/androidMain/kotlin/entity/UserAndroid.kt
actual fun User.toPlatformModel(): AndroidUser = 
    AndroidUser(id, name, email, Date(createdAt))

// shared/src/iosMain/kotlin/entity/UserIOS.kt  
actual fun User.toPlatformModel(): IOSUser =
    IOSUser(id, name, email, NSDate(timeIntervalSince1970: createdAt / 1000.0))
```

### 2. Repository Pattern

**Interface定義 (commonMain):**
```kotlin
// shared/src/commonMain/kotlin/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: String): User?
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getAllUsers(): List<User>
    suspend fun deleteUser(id: String): Result<Unit>
}

// Result wrapper for error handling
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

**プラットフォーム固有実装:**
```kotlin
// shared/src/androidMain/kotlin/repository/UserRepositoryAndroid.kt
class UserRepositoryAndroid(
    private val database: UserDatabase
) : UserRepository {
    override suspend fun getUser(id: String): User? = 
        database.userDao().getUserById(id)?.toUser()
        
    override suspend fun saveUser(user: User): Result<Unit> = 
        try {
            database.userDao().insertUser(user.toUserEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
}

// shared/src/iosMain/kotlin/repository/UserRepositoryIOS.kt  
class UserRepositoryIOS(
    private val coreDataManager: CoreDataManager
) : UserRepository {
    override suspend fun getUser(id: String): User? =
        coreDataManager.fetchUser(id)?.toUser()
        
    // ... iOS固有実装
}
```

### 3. Use Case Pattern

```kotlin
// shared/src/commonMain/kotlin/usecase/GetUserUseCase.kt
class GetUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> = 
        try {
            val user = repository.getUser(userId)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
}
```

### 4. Dependency Injection Pattern

**CommonMain Interface:**
```kotlin  
// shared/src/commonMain/kotlin/di/AppDependencies.kt
interface AppDependencies {
    val userRepository: UserRepository
    val userUseCase: GetUserUseCase
}

expect fun createAppDependencies(): AppDependencies
```

**プラットフォーム固有実装:**
```kotlin
// shared/src/androidMain/kotlin/di/AppDependenciesAndroid.kt
actual fun createAppDependencies(): AppDependencies = 
    AppDependenciesAndroid()

class AppDependenciesAndroid : AppDependencies {
    override val userRepository: UserRepository by lazy {
        UserRepositoryAndroid(DatabaseProvider.getDatabase())
    }
    
    override val userUseCase: GetUserUseCase by lazy {
        GetUserUseCase(userRepository)
    }
}
```

## 🎨 Compose Multiplatform Patterns

### 1. Screen Composable Pattern

```kotlin
// composeApp/src/commonMain/kotlin/ui/user/UserScreen.kt
@Composable
fun UserScreen(
    userId: String,
    navigator: Navigator,
    dependencies: AppDependencies = LocalDependencies.current
) {
    val userUseCase = remember { dependencies.userUseCase }
    var userState by remember { mutableStateOf<UserState>(UserState.Loading) }
    
    LaunchedEffect(userId) {
        userState = UserState.Loading
        userUseCase(userId).fold(
            onSuccess = { userState = UserState.Success(it) },
            onError = { userState = UserState.Error(it.message ?: "Unknown error") }
        )
    }
    
    when (val state = userState) {
        is UserState.Loading -> LoadingIndicator()
        is UserState.Success -> UserContent(user = state.user)
        is UserState.Error -> ErrorContent(message = state.message)
    }
}

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}
```

### 2. ViewModel Pattern (optional)

```kotlin
// composeApp/src/commonMain/kotlin/ui/user/UserViewModel.kt
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getUserUseCase(userId).fold(
                onSuccess = { user ->
                    _uiState.value = UserUiState(user = user, isLoading = false)
                },
                onError = { error ->
                    _uiState.value = UserUiState(
                        error = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }
}

data class UserUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

## 🌐 Server/API Patterns (Ktor)

### 1. Routing Pattern

```kotlin
// server/src/main/kotlin/routes/UserRoutes.kt
fun Application.configureUserRoutes(dependencies: AppDependencies) {
    routing {
        route("/api/users") {
            get("/{id}") {
                val userId = call.parameters["id"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Missing user ID")
                    return@get
                }
                
                dependencies.userUseCase(userId).fold(
                    onSuccess = { user -> call.respond(user) },
                    onError = { error -> 
                        call.respond(
                            HttpStatusCode.NotFound, 
                            mapOf("error" to error.message)
                        )
                    }
                )
            }
            
            post {
                try {
                    val user = call.receive<User>()
                    if (!user.isValid()) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user data")
                        return@post
                    }
                    
                    dependencies.userRepository.saveUser(user).fold(
                        onSuccess = { call.respond(HttpStatusCode.Created, user) },
                        onError = { error ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to error.message)
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON")
                }
            }
        }
    }
}
```

### 2. Plugin Configuration Pattern

```kotlin  
// server/src/main/kotlin/plugins/Serialization.kt
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

// server/src/main/kotlin/plugins/CORS.kt
fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // 開発環境用, 本番では制限
    }
}
```

## 🧪 Testing Patterns

### 1. 共通ロジックのテスト

```kotlin
// shared/src/commonTest/kotlin/usecase/GetUserUseCaseTest.kt
class GetUserUseCaseTest {
    private val mockRepository = MockUserRepository()
    private val useCase = GetUserUseCase(mockRepository)
    
    @Test
    fun `should return user when repository returns user`() = runTest {
        // Given
        val expectedUser = User("123", "John Doe", "john@example.com")
        mockRepository.setUser(expectedUser)
        
        // When  
        val result = useCase("123")
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedUser, (result as Result.Success).data)
    }
    
    @Test
    fun `should return error when user not found`() = runTest {
        // Given
        mockRepository.setUser(null)
        
        // When
        val result = useCase("123")
        
        // Then
        assertTrue(result is Result.Error)
    }
}

class MockUserRepository : UserRepository {
    private var user: User? = null
    
    fun setUser(user: User?) { this.user = user }
    
    override suspend fun getUser(id: String): User? = user
    override suspend fun saveUser(user: User): Result<Unit> = Result.Success(Unit)
    override suspend fun getAllUsers(): List<User> = listOfNotNull(user)
    override suspend fun deleteUser(id: String): Result<Unit> = Result.Success(Unit)
}
```

### 2. UI テスト

```kotlin
// composeApp/src/commonTest/kotlin/ui/UserScreenTest.kt
class UserScreenTest {
    @Test
    fun userScreen_loading_showsLoadingIndicator() {
        val mockDependencies = MockAppDependencies(loading = true)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(userId = "123", navigator = MockNavigator())
            }
        }
        
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
    
    @Test  
    fun userScreen_success_showsUserContent() {
        val testUser = User("123", "John Doe", "john@example.com")
        val mockDependencies = MockAppDependencies(user = testUser)
        
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(userId = "123", navigator = MockNavigator())
            }
        }
        
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }
}
```

### 3. API テスト

```kotlin
// server/src/test/kotlin/routes/UserRoutesTest.kt
class UserRoutesTest {
    private lateinit var testApplication: TestApplication
    
    @Before
    fun setup() {
        testApplication = TestApplication {
            configureUserRoutes(MockAppDependencies())
        }
    }
    
    @Test
    fun `GET user returns user data`() = testApplication.test {
        // Given
        val expectedUser = User("123", "John Doe", "john@example.com")
        
        // When
        val response = client.get("/api/users/123")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val user = response.body<User>()
        assertEquals(expectedUser, user)
    }
    
    @Test
    fun `POST user creates new user`() = testApplication.test {
        // Given
        val newUser = User("456", "Jane Doe", "jane@example.com")
        
        // When
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(newUser)
        }
        
        // Then
        assertEquals(HttpStatusCode.Created, response.status)
    }
}
```

## 🔧 Build & Configuration Patterns

### 1. gradle.properties設定
```properties
# Kotlin Multiplatform
kotlin.mpp.enableCInteropCommonization=true
kotlin.native.ignoreDisabledTargets=true

# Compose Multiplatform  
compose.experimental.uikit.enabled=true
compose.experimental.jscanvas.enabled=true

# Ktor
ktor.deployment.port=8080
```

### 2. 共通Dependencies管理

```kotlin
// build.gradle.kts (project root)
val kotlinVersion by extra("1.9.20")
val ktorVersion by extra("2.3.5")
val composeVersion by extra("1.5.4")

// shared/build.gradle.kts
dependencies {
    commonMain {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    }
    
    commonTest {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    }
}
```

## ⚠️ 注意事項・制約

### 1. プラットフォーム固有実装の原則
- `expect/actual`は最小限に留める
- プラットフォーム固有機能は対応するsourceSetに配置
- 共通インターフェースで抽象化

### 2. 依存関係管理
- composeApp → shared (OK)
- server → shared (OK)  
- shared → composeApp/server (NG)

### 3. テスト戦略
- 各レイヤーで独立したユニットテスト
- モック使用による依存関係分離
- 統合テストは最小限（重要なフローのみ）

---

**更新履歴**: Agent実装時に新しいパターンが発見された場合、このファイルに追記してください。