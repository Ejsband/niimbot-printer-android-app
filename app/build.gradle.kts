plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ru.odinesina.niimbot"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.odinesina.niimbot"
        minSdk = 26
        targetSdk = 33
        versionName = "1.0"
        setProperty("archivesBaseName", "niimbot($versionName)")
    }

    signingConfigs {
        create("release") {
            keyAlias = "habize"
            keyPassword = "Habize931"
            storeFile = file("D:\\signature.jks")
            storePassword = "Habize931"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
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
        viewBinding = true
    }
}

dependencies {

    implementation(files("./libs/3.1.8-release.aar"))
    implementation(files("./libs/image-2.0.10-release.aar"))

    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}