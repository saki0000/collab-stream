# Testing Patterns & Strategies

Kotlin Multiplatformプロジェクトでのテスト作成パターンとベストプラクティス。各レイヤーで必須となるユニットテスト作成の指針です。

## 🎯 テスト戦略概要

### 基本方針
- **各レイヤーで必須**: shared/compose/server層すべてでユニットテスト作成
- **Mock中心**: 外部依存を排除した独立テスト
- **実行速度重視**: 並行実行可能なテスト設計
- **カバレッジ目標**: 80%以上（ビジネスロジックは90%以上）

### テストピラミッド
```
        E2E Tests (5%)
      ↗               ↖
Integration Tests (15%)
  ↗                     ↖  
Unit Tests (80%)
```

## 🧪 Shared層テストパターン

### 1. Entity/Data Classテスト

```kotlin
// shared/src/commonTest/kotlin/entity/UserTest.kt
class UserTest {
    
    @Test
    fun `User creation with valid data should succeed`() {
        // Given
        val name = "John Doe"
        val email = "john@example.com"
        val timestamp = 1640995200000L
        
        // When
        val user = User(
            id = "123",
            name = name,
            email = email,
            createdAt = timestamp
        )
        
        // Then
        assertEquals("123", user.id)
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        assertEquals(timestamp, user.createdAt)
    }
    
    @Test
    fun `isValid should return true for valid user`() {
        // Given
        val user = User("123", "John Doe", "john@example.com")
        
        // When
        val isValid = user.isValid()
        
        // Then
        assertTrue(isValid)
    }
    
    @Test
    fun `isValid should return false for invalid email`() {
        // Given
        val user = User("123", "John Doe", "invalid-email")
        
        // When
        val isValid = user.isValid()
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `isValid should return false for blank name`() {
        // Given
        val user = User("123", "", "john@example.com")
        
        // When
        val isValid = user.isValid()
        
        // Then
        assertFalse(isValid)
    }
}
```

### 2. Repository/Use Caseテスト

```kotlin
// shared/src/commonTest/kotlin/usecase/GetUserUseCaseTest.kt
class GetUserUseCaseTest {
    private val mockRepository = MockUserRepository()
    private val useCase = GetUserUseCase(mockRepository)
    
    @Test  
    fun `invoke should return success when user exists`() = runTest {
        // Given
        val expectedUser = User("123", "John Doe", "john@example.com")
        mockRepository.setGetUserResult(expectedUser)
        
        // When
        val result = useCase("123")
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedUser, (result as Result.Success).data)
    }
    
    @Test
    fun `invoke should return error when user not found`() = runTest {
        // Given
        mockRepository.setGetUserResult(null)
        
        // When
        val result = useCase("nonexistent")
        
        // Then
        assertTrue(result is Result.Error)
        assertTrue(result.exception.message!!.contains("User not found"))
    }
    
    @Test
    fun `invoke should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = RuntimeException("Database error")
        mockRepository.setGetUserException(expectedException)
        
        // When
        val result = useCase("123")
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals(expectedException, result.exception)
    }
}

// Test fixture
class MockUserRepository : UserRepository {
    private var getUserResult: User? = null
    private var getUserException: Exception? = null
    private var saveUserResult: Result<Unit> = Result.Success(Unit)
    
    fun setGetUserResult(user: User?) { 
        this.getUserResult = user 
        this.getUserException = null
    }
    
    fun setGetUserException(exception: Exception) {
        this.getUserException = exception
        this.getUserResult = null
    }
    
    fun setSaveUserResult(result: Result<Unit>) {
        this.saveUserResult = result
    }
    
    override suspend fun getUser(id: String): User? {
        getUserException?.let { throw it }
        return getUserResult
    }
    
    override suspend fun saveUser(user: User): Result<Unit> = saveUserResult
    
    override suspend fun getAllUsers(): List<User> = listOfNotNull(getUserResult)
    
    override suspend fun deleteUser(id: String): Result<Unit> = Result.Success(Unit)
}
```

### 3. Platform-specificテスト

```kotlin
// shared/src/commonTest/kotlin/platform/PlatformDependencyTest.kt
class PlatformDependencyTest {
    
    @Test
    fun `createAppDependencies should return valid dependencies`() {
        // When
        val dependencies = createAppDependencies()
        
        // Then
        assertNotNull(dependencies.userRepository)
        assertNotNull(dependencies.userUseCase)
    }
}

// shared/src/androidUnitTest/kotlin/repository/UserRepositoryAndroidTest.kt
class UserRepositoryAndroidTest {
    private val mockDatabase = mockk<UserDatabase>()
    private val mockDao = mockk<UserDao>()
    private val repository = UserRepositoryAndroid(mockDatabase)
    
    @Before
    fun setup() {
        every { mockDatabase.userDao() } returns mockDao
    }
    
    @Test
    fun `getUser should return user from database`() = runTest {
        // Given
        val userEntity = UserEntity("123", "John Doe", "john@example.com", 1640995200000L)
        every { mockDao.getUserById("123") } returns userEntity
        
        // When
        val result = repository.getUser("123")
        
        // Then
        assertNotNull(result)
        assertEquals("123", result!!.id)
        assertEquals("John Doe", result.name)
    }
    
    @Test
    fun `saveUser should insert user to database`() = runTest {
        // Given
        val user = User("123", "John Doe", "john@example.com")
        every { mockDao.insertUser(any()) } just Runs
        
        // When
        val result = repository.saveUser(user)
        
        // Then
        assertTrue(result is Result.Success)
        verify { mockDao.insertUser(any()) }
    }
}
```

## 🎨 Compose層テストパターン

### 1. Screen Composableテスト

```kotlin
// composeApp/src/commonTest/kotlin/ui/UserScreenTest.kt
@OptIn(ExperimentalTestApi::class)
class UserScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `UserScreen loading state shows loading indicator`() {
        // Given
        val mockDependencies = MockAppDependencies().apply {
            setGetUserUseCaseResult(null) // Still loading
        }
        
        // When
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(
                    userId = "123",
                    navigator = MockNavigator()
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
    
    @Test
    fun `UserScreen success state shows user information`() {
        // Given
        val testUser = User("123", "John Doe", "john@example.com")
        val mockDependencies = MockAppDependencies().apply {
            setGetUserUseCaseResult(Result.Success(testUser))
        }
        
        // When
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(
                    userId = "123", 
                    navigator = MockNavigator()
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }
    
    @Test
    fun `UserScreen error state shows error message`() {
        // Given
        val mockDependencies = MockAppDependencies().apply {
            setGetUserUseCaseResult(Result.Error(Exception("Network error")))
        }
        
        // When
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(
                    userId = "123",
                    navigator = MockNavigator()
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
    
    @Test
    fun `UserScreen back button click should navigate back`() {
        // Given
        val mockNavigator = MockNavigator()
        val mockDependencies = MockAppDependencies().apply {
            setGetUserUseCaseResult(Result.Success(User("123", "John", "john@example.com")))
        }
        
        // When
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDependencies provides mockDependencies) {
                UserScreen(userId = "123", navigator = mockNavigator)
            }
        }
        
        composeTestRule.onNodeWithTag("back_button").performClick()
        
        // Then
        assertTrue(mockNavigator.backCalled)
    }
}

// Test fixtures
class MockAppDependencies : AppDependencies {
    private var getUserUseCaseResult: Result<User>? = null
    
    fun setGetUserUseCaseResult(result: Result<User>?) {
        this.getUserUseCaseResult = result
    }
    
    override val userRepository: UserRepository = MockUserRepository()
    
    override val userUseCase: GetUserUseCase = object : GetUserUseCase(userRepository) {
        override suspend fun invoke(userId: String): Result<User> {
            return getUserUseCaseResult ?: Result.Error(Exception("Loading"))
        }
    }
}

class MockNavigator : Navigator {
    var backCalled = false
    
    override fun back() {
        backCalled = true
    }
    
    override fun navigateTo(route: String) {}
}
```

### 2. ViewModelテスト

```kotlin
// composeApp/src/commonTest/kotlin/ui/UserViewModelTest.kt
class UserViewModelTest {
    private val mockGetUserUseCase = mockk<GetUserUseCase>()
    private val viewModel = UserViewModel(mockGetUserUseCase)
    
    @Test
    fun `loadUser should update state to loading then success`() = runTest {
        // Given
        val expectedUser = User("123", "John Doe", "john@example.com")
        coEvery { mockGetUserUseCase("123") } returns Result.Success(expectedUser)
        
        val states = mutableListOf<UserUiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }
        
        // When
        viewModel.loadUser("123")
        
        // Wait for state changes
        advanceUntilIdle()
        
        // Then
        assertTrue(states.any { it.isLoading && it.user == null }) // Loading state
        assertTrue(states.any { it.user == expectedUser && !it.isLoading }) // Success state
        
        job.cancel()
    }
    
    @Test
    fun `loadUser should handle error state`() = runTest {
        // Given
        val errorMessage = "User not found"
        coEvery { mockGetUserUseCase("123") } returns Result.Error(Exception(errorMessage))
        
        val states = mutableListOf<UserUiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }
        
        // When
        viewModel.loadUser("123")
        advanceUntilIdle()
        
        // Then
        assertTrue(states.any { it.error == errorMessage && !it.isLoading })
        
        job.cancel()
    }
}
```

## 🌐 Server層テストパターン

### 1. API Routesテスト

```kotlin
// server/src/test/kotlin/routes/UserRoutesTest.kt
class UserRoutesTest {
    private val mockDependencies = MockAppDependencies()
    
    @Test
    fun `GET users by id returns user when exists`() = testApplication {
        // Given
        application {
            configureUserRoutes(mockDependencies)
        }
        
        val expectedUser = User("123", "John Doe", "john@example.com")
        mockDependencies.setGetUserUseCaseResult(Result.Success(expectedUser))
        
        // When
        val response = client.get("/api/users/123")
        
        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val user = response.body<User>()
        assertEquals(expectedUser, user)
    }
    
    @Test
    fun `GET users returns 404 when user not found`() = testApplication {
        // Given
        application {
            configureUserRoutes(mockDependencies)
        }
        
        mockDependencies.setGetUserUseCaseResult(Result.Error(Exception("User not found")))
        
        // When
        val response = client.get("/api/users/nonexistent")
        
        // Then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
    
    @Test
    fun `GET users returns 400 when id parameter missing`() = testApplication {
        // Given
        application {
            configureUserRoutes(mockDependencies) 
        }
        
        // When
        val response = client.get("/api/users/")
        
        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
    
    @Test
    fun `POST users creates user with valid data`() = testApplication {
        // Given
        application {
            configureUserRoutes(mockDependencies)
        }
        
        val newUser = User("456", "Jane Doe", "jane@example.com")
        mockDependencies.setSaveUserResult(Result.Success(Unit))
        
        // When
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(newUser)
        }
        
        // Then
        assertEquals(HttpStatusCode.Created, response.status)
        val responseUser = response.body<User>()
        assertEquals(newUser, responseUser)
    }
    
    @Test
    fun `POST users returns 400 with invalid user data`() = testApplication {
        // Given
        application {
            configureUserRoutes(mockDependencies)
        }
        
        val invalidUser = User("456", "", "invalid-email") // Invalid data
        
        // When
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json) 
            setBody(invalidUser)
        }
        
        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
```

### 2. Pluginテスト

```kotlin
// server/src/test/kotlin/plugins/SerializationTest.kt
class SerializationTest {
    
    @Test
    fun `JSON serialization configuration works correctly`() = testApplication {
        // Given
        application {
            configureSerialization()
            routing {
                get("/test") {
                    call.respond(User("123", "Test User", "test@example.com"))
                }
                post("/test") {
                    val user = call.receive<User>()
                    call.respond(user)
                }
            }
        }
        
        // When - GET request
        val getResponse = client.get("/test")
        
        // Then
        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertEquals("application/json; charset=UTF-8", getResponse.contentType().toString())
        
        // When - POST request
        val testUser = User("456", "Jane Doe", "jane@example.com")
        val postResponse = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody(testUser)
        }
        
        // Then
        assertEquals(HttpStatusCode.OK, postResponse.status)
        val responseUser = postResponse.body<User>()
        assertEquals(testUser, responseUser)
    }
}
```

## 🔄 統合テストパターン

### 1. End-to-End APIテスト

```kotlin
// server/src/test/kotlin/integration/UserFlowIntegrationTest.kt
class UserFlowIntegrationTest {
    
    @Test
    fun `complete user flow should work end to end`() = testApplication {
        // Given
        application {
            configureUserRoutes(createTestAppDependencies())
        }
        
        val newUser = User("integration-123", "Integration User", "integration@example.com")
        
        // When - Create user
        val createResponse = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(newUser)
        }
        
        // Then - User created successfully
        assertEquals(HttpStatusCode.Created, createResponse.status)
        
        // When - Get created user
        val getResponse = client.get("/api/users/${newUser.id}")
        
        // Then - User retrieved successfully
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val retrievedUser = getResponse.body<User>()
        assertEquals(newUser, retrievedUser)
        
        // When - Update user (if endpoint exists)
        val updatedUser = newUser.copy(name = "Updated Name")
        val updateResponse = client.put("/api/users/${newUser.id}") {
            contentType(ContentType.Application.Json)
            setBody(updatedUser)
        }
        
        // Then - User updated successfully  
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        
        // When - Delete user
        val deleteResponse = client.delete("/api/users/${newUser.id}")
        
        // Then - User deleted successfully
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        
        // When - Try to get deleted user
        val getDeletedResponse = client.get("/api/users/${newUser.id}")
        
        // Then - User not found
        assertEquals(HttpStatusCode.NotFound, getDeletedResponse.status)
    }
}
```

## 🏃 テスト実行戦略

### Gradle設定

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("app.cash.turbine:turbine:1.0.0") // StateFlow testing
        }
        
        androidUnitTest.dependencies {
            implementation("io.mockk:mockk:1.13.8")
            implementation("androidx.arch.core:core-testing:2.2.0")
        }
        
        iosTest.dependencies {
            // iOS specific test dependencies
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
    
    // Parallel execution
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    
    // Memory settings
    minHeapSize = "256m"
    maxHeapSize = "2g"
}
```

### テスト実行コマンド
```bash
# 全てのテスト実行
./gradlew test

# レイヤー別テスト実行
./gradlew :shared:test          # Shared layer
./gradlew :composeApp:test      # Compose layer  
./gradlew :server:test          # Server layer

# 並行実行
./gradlew test --parallel

# カバレッジ付き実行  
./gradlew test jacocoTestReport
./gradlew koverHtmlReport       # Kotlin Multiplatform用
```

## 📊 テストカバレッジ目標

### レイヤー別目標
```yaml
shared_layer:
  unit_test_coverage: 90%
  critical_paths: 100%
  entities: 85%
  usecases: 95%
  repositories: 80%

compose_layer:
  ui_test_coverage: 70%
  screen_tests: 80%
  viewmodel_tests: 85%
  navigation_tests: 60%

server_layer:
  api_test_coverage: 85%
  endpoint_tests: 90%
  integration_tests: 70%
  error_handling: 95%
```

### 品質ゲート
```yaml
minimum_requirements:
  overall_coverage: 80%
  build_success: true
  no_failing_tests: true
  no_flaky_tests: true
  execution_time: "<5 minutes"
```

---

**実装ガイド**: 各レイヤー実装時に対応するテストパターンを必ず適用し、実装とテストを同時に作成してください。