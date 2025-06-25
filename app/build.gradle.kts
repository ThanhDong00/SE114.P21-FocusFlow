plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp") // Áp dụng plugin KSP ở đây
    id("com.google.dagger.hilt.android") // Áp dụng plugin Hilt ở đây
}

android {
    namespace = "com.dong.focusflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dong.focusflow"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core) // Core Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // DataStore (Preferences DataStore)
    implementation(libs.androidx.datastore.preferences)

    // Hilt Dependencies
    implementation(libs.hilt.android) // Thêm dependency Hilt Android
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room Dependencies (ví dụ)
    implementation(libs.androidx.room.runtime) // Thêm dependency Room Runtime
    ksp(libs.androidx.room.compiler) // Thêm annotation processor cho Room (sử dụng ksp)
    implementation(libs.androidx.room.ktx) // Room KTX extensions (cho coroutines)

    implementation(libs.vico.compose)
    implementation(libs.vico.core) // Quan trọng: Đảm bảo dòng này có mặt
    implementation(libs.vico.views)

}