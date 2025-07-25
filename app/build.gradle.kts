plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.1.20-2.0.1"
    id("com.google.gms.google-services")

}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
android {
    namespace = "com.example.weasel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weasel"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.2"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    lint {
        checkReleaseBuilds = true
        abortOnError = false
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation("androidx.startup:startup-runtime:1.2.0")

    // NewPipe Extractor
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.6")

    // Media3 for audio playback
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
    implementation("androidx.media3:media3-session:1.7.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.work:work-runtime-ktx:2.10.2")

    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx")

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
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")


    implementation("com.kmpalette:kmpalette-core:3.1.0")

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // WorkManager

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}