plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android) <-- EZT TÖRÖLD KI
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.mobildrontesztprojekt"
    compileSdk = 36



    defaultConfig {
        applicationId = "com.example.mobildrontesztprojekt"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

// kotlinOptions { jvmTarget } helyett – AGP 9.x / Kotlin 2.x kompatibilis

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Activity + Navigation
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

ksp {
    arg("room.incremental", "true")
}

// Ez mondja meg az Androidnak, hogy hol keresse a KSP által generált fájlokat
android {
    sourceSets {
        getByName("main") {
            java.srcDir("build/generated/ksp/main/kotlin")
        }
        getByName("debug") {
            java.srcDir("build/generated/ksp/debug/kotlin")
        }
        getByName("release") {
            java.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}
