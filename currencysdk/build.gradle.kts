plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.currencysdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Retrofit – turns HTTP endpoints into Kotlin functions
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp – the HTTP client Retrofit uses under the hood;
    // the logging interceptor prints request/response in Logcat (handy for debugging)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Kotlin Coroutines – lets us call network functions with "suspend"
    // so they don't block the main thread
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}