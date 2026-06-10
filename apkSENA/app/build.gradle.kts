plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "co.edu.sena"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.edu.sena"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // OkHttp para llamadas API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // WorkManager para sincronización offline
    implementation("androidx.work:work-runtime:2.9.1")
    implementation(libs.foundation)
    implementation(libs.firebase.crashlytics.buildtools)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
