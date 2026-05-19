plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.cyber.omnigrid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cyber.omnigrid"
        minSdk = 26 // Necesario para la API de Crypto moderna
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-alpha"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // Dependencias base actuales
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    // =================================================================
    // NUEVAS DEPENDENCIAS PARA SOLUCIONAR ERRORES DE COMPILACIÓN (#69)
    // =================================================================
    
    // Jetpack Navigation Compose (Soluciona rememberNavController, NavHost, etc.)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database (Soluciona la persistencia local y OmniGridDatabase)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Lifecycle ViewModel Compose (Soluciona el uso de la función inline viewModel())
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}
