import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties
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
    kotlin("plugin.serialization") version "2.2.20"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
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
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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
    add("kspIosX64", libs.androidx.room.compiler)
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

// local.properties ファイルを読み込む
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

buildkonfig {
    packageName = "org.exampl.project"
    defaultConfigs {
        val apiKey = System.getenv("API_KEY") ?: localProperties.getProperty("API_KEY")
        val twitchApiKey = System.getenv("TWITCH_API_KEY") ?: localProperties.getProperty("TWITCH_API_KEY")
        val twitchClientId = System.getenv("TWITCH_CLIENT_ID") ?: localProperties.getProperty("TWITCH_CLIENT_ID")
        buildConfigField(FieldSpec.Type.STRING, "API_KEY", "$apiKey")
        buildConfigField(FieldSpec.Type.STRING, "TWITCH_API_KEY", "$twitchApiKey")
        buildConfigField(FieldSpec.Type.STRING, "TWITCH_CLIENT_ID", "$twitchClientId")
    }
}
