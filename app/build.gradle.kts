plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)  // 🔴 necesario para Room
    alias(libs.plugins.kotlin.android) // 🔴 ESTE FALTABA

}

android {
    namespace = "com.alan.routineos"
    compileSdk = 36

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.alan.routineos"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


kotlin {
    jvmToolchain(17)
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    /// Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    /// Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    /// Room (base de datos)
    implementation("androidx.room:room-runtime:2.6.1")
    // datastore (persistencia)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //Coroutines (si no viene ya en libs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //WorkManager (sync engine)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    //Retrofit (backend futuro)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Logging (muy útil)
    implementation("com.jakewharton.timber:timber:5.0.1")
}

