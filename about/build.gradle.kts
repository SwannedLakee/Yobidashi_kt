/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool
import property.LibraryVersion

plugins {
  id("com.android.library")
  id("kotlin-android")
  id("jacoco.definition")
  alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.about"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libraries.versions.kotlinComposeCompilerExtension.get()
    }
    testOptions.unitTests.isIncludeAndroidResources = true
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(path = ":lib"))
    implementation(project(path = ":ui"))
    implementation(project(path = ":licenses"))

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    implementation(libraries.activityCompose)
    implementation(libraries.composeMaterial3)

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("androidx.compose.ui:ui-test-junit4-android:1.8.0")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.8.0")
}
