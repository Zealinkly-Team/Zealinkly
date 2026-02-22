plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.elderui"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.elderui"
        minSdk = 30
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Network
    implementation(libs.retrofit)
    implementation("com.google.code.gson:gson:2.10.1") // 添加 Gson 库
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // 添加 Gson 转换器
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Data storage
    implementation(libs.androidx.datastore)

    // Permissions
    implementation(libs.accompanist.permissions)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}