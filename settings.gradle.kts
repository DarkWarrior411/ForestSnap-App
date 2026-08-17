import java.util.Properties

// Plugin repositories configuration
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Load local configuration properties if present, fallback to environment variables
val localProperties = Properties()
val localPropertiesFile = File(rootDir, "local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val mapboxToken = localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: System.getenv("MAPBOX_DOWNLOADS_TOKEN") ?: ""

// Dependency resolution repositories including private Mapbox Maven endpoint
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = mapboxToken
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "ForestSnap"
include(":app")
