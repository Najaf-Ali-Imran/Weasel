// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // This line is now corrected to use "kotlin" instead of "jetbrains"
    alias(libs.plugins.kotlin.android) apply false
}