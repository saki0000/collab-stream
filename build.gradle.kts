plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi) apply false
}

dependencies {
    kover(projects.shared)
    kover(projects.composeApp)
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*Fragment",
                    "*Fragment\$*",
                    "*Activity", 
                    "*Activity\$*",
                    "*.databinding.*",
                    "*.BuildConfig",
                    "*\$WhenMappings",
                    "*ComposableSingletons*",
                    "*_Impl",
                    "*_Impl\$*"
                )
                packages(
                    "*.build.*"
                )
            }
        }
        total {
            html {
                onCheck = false
                title = "CollabStream Test Coverage Report"
            }
            xml {
                onCheck = false
            }
        }
    }
}
