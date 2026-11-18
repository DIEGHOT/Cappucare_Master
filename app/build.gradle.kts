import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

// Credenciales de firma del APK (archivo keystore.properties en la raíz del proyecto)
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

// Dirección de los microservicios. Emulador: 10.0.2.2 (el equipo anfitrión).
// Dispositivo físico: IP del PC en la red Wi-Fi (se cambia en gradle.properties).
val hostBackend = providers.gradleProperty("cappucare.backendHost").getOrElse("10.0.2.2")

android {
    namespace = "cl.duoc.cappucare"
    compileSdk = 34

    defaultConfig {
        applicationId = "cl.duoc.cappucare"
        minSdk = 30
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PRODUCTOS_URL", "\"http://$hostBackend:8081/\"")
        buildConfigField("String", "MOVIMIENTOS_URL", "\"http://$hostBackend:8082/\"")
        buildConfigField("String", "INDICADORES_URL", "\"https://mindicador.cl/\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile", "keystore/cappucare-release.jks"))
            storePassword = propiedadesFirma.getProperty("storePassword")
            keyAlias = propiedadesFirma.getProperty("keyAlias")
            keyPassword = propiedadesFirma.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    lint {
        abortOnError = false
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    // MVVM + ciclo de vida
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegación
    implementation(libs.androidx.navigation.compose)

    // Room (caché local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Red: microservicios Spring Boot y API externa
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Imágenes (foto del insumo)
    implementation(libs.coil.compose)

    // Escáner de códigos de barras: CameraX + ML Kit
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Pruebas unitarias
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Pruebas instrumentadas
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
