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
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/codelab.versions.toml"))
        }
    }
}

rootProject.name = "android-codelab-master"

include(":app")
include(":domain")
include(":data")
include(":geofence")
