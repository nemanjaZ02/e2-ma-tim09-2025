plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.e2_ma_tim09_2025.questify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.e2_ma_tim09_2025.questify"
        minSdk = 34
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.room.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.google.guava)
    implementation(libs.hilt.work)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    annotationProcessor(libs.hilt.compiler.work)
    implementation(libs.firebase.firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    annotationProcessor(libs.room.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    api(libs.kizitonwose.calendar)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")

}