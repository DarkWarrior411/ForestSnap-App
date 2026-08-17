// Top-level build configuration specifying plugins shared across all sub-modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}

// Clean task to clear root build artifacts.
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
