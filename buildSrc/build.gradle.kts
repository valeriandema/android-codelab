import org.gradle.api.JavaVersion

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Coordinates mirror gradle/codelab.versions.toml. Hardcoded (no version catalog in buildSrc) so the
// kotlin-dsl plugin does not generate accessor classes that could collide with consuming modules.
dependencies {
    implementation("com.android.tools.build:gradle:8.12.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.10-2.0.2")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.57.1")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.10")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "codelab.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "codelab.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("hilt") {
            id = "codelab.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("androidRoom") {
            id = "codelab.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "codelab.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
