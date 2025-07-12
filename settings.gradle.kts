pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // This line is required to find and download NewPipeExtractor.
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "Weasel"
include(":app")