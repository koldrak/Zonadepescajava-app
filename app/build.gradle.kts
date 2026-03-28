plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.daille.zonadepescajava_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.daille.zonadepescajava_app"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.11"
        /*1. correccion para preguntar usuario y pais al inicio de la app
*/
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

    sourceSets {
        getByName("main") {
            assets.srcDir("src/main/res/drawable/img")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.viewmodel)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}