plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("codelab.android.library")
    id("codelab.hilt")
    id("codelab.android.room")
}

android {
    namespace = "com.sap.codelab.data"
}

dependencies {
    implementation(project(":domain"))

    testImplementation(libs.junit)
}
