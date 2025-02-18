plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.studymate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.studymate"
        minSdk = 23
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
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    // Firebase BOM (preporučena verzija)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    // Firebase Auth i Analytics
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("com.google.firebase:firebase-database-ktx:20.2.1")


    // Google Play Services - koristi ove verzije
    implementation("com.google.android.gms:play-services-measurement-api:22.2.0")
    implementation("com.google.android.gms:play-services-measurement-impl:22.2.0")

    // AndroidX i UI biblioteke
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation ("com.google.android.material:material:1.5.0")
    // Test zavisnosti
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp za logovanje HTTP zahteva
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // GSON za JSON obradu
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("com.aallam.openai:openai-client:3.2.0") // OpenAI SDK
    implementation("io.ktor:ktor-client-okhttp:2.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation("androidx.activity:activity-ktx:1.8.0")

}

