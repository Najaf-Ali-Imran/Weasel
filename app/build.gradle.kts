plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.0-1.0.24"
}

android {
    namespace = "com.example.weasel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weasel"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Use Java 8 for desugaring
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // Kotlin‑DSL setter for core library desugaring
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        // Match Java compatibility
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core/KTX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // NewPipe Extractor
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.6")

    // Media3 for audio playback
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
    implementation("androidx.media3:media3-session:1.7.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Coil image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Guava
    implementation("com.google.guava:guava:33.0.0-android")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    // Navigation support in Compose
    implementation("androidx.navigation:navigation-compose:2.9.1")

    // Accompanist, NewPipe, Media3, etc…
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.6")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // IMPORTANT: Use the NIO version of desugaring for NewPipe compatibility
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}