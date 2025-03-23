plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.ble"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.hilt.android.core)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.hilt.android.testing)
    implementation(libs.androidx.annotation)
    ksp(libs.hilt.compiler)


    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.inline)
    androidTestImplementation(libs.androidx.espresso.core)

    // To use the androidx.test.core APIs
    androidTestImplementation(libs.androidx.core.v)
    // Kotlin extensions for androidx.test.core
    androidTestImplementation(libs.core.ktx)

    // Kotlin extensions for androidx.test.ext.junit
    androidTestImplementation(libs.androidx.junit.ktx)

    // To use the Truth Extension APIs
    androidTestImplementation(libs.androidx.truth)

    // To use the androidx.test.runner APIs
    androidTestImplementation(libs.androidx.runner)

    // To use android test orchestrator
    androidTestUtil(libs.androidx.orchestrator)
    testImplementation(kotlin("test"))
}