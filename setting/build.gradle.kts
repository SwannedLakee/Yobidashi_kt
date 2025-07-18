import property.LibraryVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.setting"

    compileSdkVersion(property.BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(property.BuildTool.minSdk)
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<com.android.build.gradle.tasks.GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":data"))
    implementation(project(":calendar"))
    implementation(project(":web"))
    implementation(project(":ui"))
    implementation(project(":search"))
    implementation(project(":editor"))

    implementation(libraries.composeMaterial3)
    implementation("androidx.core:core-ktx:1.7.0")
    implementation(libraries.activityCompose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")
    implementation(libraries.exifinterface)
    implementation(libraries.timber)
    implementation("com.godaddy.android.colorpicker:compose-color-picker:0.4.2")

    testImplementation("junit:junit:${property.LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${property.LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
    testImplementation(testLibraries.bytebuddy)
}
