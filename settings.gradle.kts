pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Resolves Java toolchains (incl. the daemon JVM pinned in gradle/gradle-daemon-jvm.properties),
    // downloading a matching JDK when none is installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
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
