import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.devtools.ksp")    // ROOM
}
val properties = Properties()
properties.load(FileInputStream(rootProject.file("local.properties")))

android {
    namespace = "com.example.myapiapplication"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.myapiapplication"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NAVER_CLIENT_ID", properties.getProperty("naver_client_id"))
        buildConfigField("String", "NAVER_CLIENT_SECRET", properties.getProperty("naver_client_secret"))
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["GOOGLE_MAP_API_KEY"] = properties["google_map_api_key"] as String

        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders["GOOGLE_MAP_API_KEY"] = properties["google_map_api_key"] as String
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    viewBinding {
        enable=true
    }
    buildFeatures{
        buildConfig=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //    ROOM
    val room_version = "2.5.0"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    implementation("androidx.room:room-ktx:$room_version")

    ksp("androidx.room:room-compiler:$room_version")

    // GooglePlayService Location Library
    implementation("com.google.android.gms:play-services-location:21.0.1")
    //GoogleMap
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

}