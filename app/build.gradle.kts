plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}



android {
    namespace = "com.example.arcadebits"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.arcadebits"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding{
        enable=true
    }
}

dependencies {


    //se utiliza principalmente para cargar y mostrar imágenes de manera eficiente en aplicaciones Android.
    //Glide es una potente biblioteca de carga de imágenes desarrollada por Bumptech. Es especialmente útil para trabajar con imágenes desde:
    // -URLs (internet) -recursos locales -archivos -URIs, etc.
    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.google.code.gson:gson:2.9.1")


    implementation("com.tbuonomo:dotsindicator:5.0")


    implementation("androidx.drawerlayout:drawerlayout:1.2.0")



    //apartir de los apuntes
    // Activity
    implementation("androidx.activity:activity-ktx:1.10.0")
    //implementation ("androidx.activity:activity-ktx:1.6.1")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")


    //FIREBASE
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation(libs.firebase.database)

    // Firebase Auth & Firestore
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")


    // Facebook Login SDK
    implementation("com.facebook.android:facebook-login:16.0.6")
    implementation("com.facebook.android:facebook-core:16.0.6")
    implementation("com.facebook.android:facebook-common:16.0.6")

    implementation("com.facebook.android:facebook-android-sdk:latest.release")


    implementation("com.google.android.material:material:1.12.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.analytics.impl)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}