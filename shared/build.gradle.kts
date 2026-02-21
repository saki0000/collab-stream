import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.konfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain.dependencies {
            // HTTP client for API calls
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Date and time handling
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
            // Dependency Injection
            implementation(libs.koin.core)
            // Room KMP (Database)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            // RevenueCat KMP SDK (Android/iOSのみ対応)
            implementation(libs.purchases.kmp.core)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            // RevenueCat KMP SDK (Android/iOSのみ対応)
            implementation(libs.purchases.kmp.core)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.java)
        }
    }
}

// Room schema directory for migration support
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP configuration for Room compiler (Android and iOS targets)
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

android {
    namespace = "org.example.project.shared"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
}

ktlint {
    version.set("1.6.0")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// BuildKonfig: APIキーをクライアントに含めない（ADR-005 Phase 2）
// サーバーAPI経由でYouTube/Twitch APIを呼び出すため、クライアント側のAPIキー定義は不要
buildkonfig {
    packageName = "org.exampl.project"
    defaultConfigs {
        // 将来的に必要な定数があればここに追加
        // 現在はサーバーAPI経由のため、APIキーは不要
    }
}
