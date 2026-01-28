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
        versionCode = 4
        versionName = "1.4"
/*1. Correccion de bonificaciones y ui
  2. Incorporación de alerta de romper top global
  3. Animación de carniboros
  4. Implementacion de sonidos temáticos para ballenas
  5. Se agrego tutorial de mareas
  6. Reestructuracion de UI de juego
  7. Reestructuracion de panel de dados y mazos
  8. Actualizacion de tipos de cartas
  9. Ajuste de habilidades de "Pulpo, Sepia y cangrejo herradura"
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