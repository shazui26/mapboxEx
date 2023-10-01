plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.map.mapboxex"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.map.mapboxex"
        minSdk = 29
        targetSdk = 33
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

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Mapbox Maps SDK for Android dependency
    implementation ("com.mapbox.search:autofill:1.0.0-rc.6")
    implementation ("com.mapbox.search:discover:1.0.0-rc.6")
    implementation ("com.mapbox.search:place-autocomplete:1.0.0-rc.6")
    implementation ("com.mapbox.search:offline:1.0.0-rc.6")
    implementation ("com.mapbox.search:mapbox-search-android:1.0.0-rc.6")
    implementation ("com.mapbox.search:mapbox-search-android-ui:1.0.0-rc.6")
    implementation ("com.mapbox.maps:android:10.16.0")
    implementation ("com.mapbox.navigation:ui-dropin:2.16.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


}