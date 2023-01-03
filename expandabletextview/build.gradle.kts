plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 19
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("release"))
            }
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation ("com.facebook.fbui.textlayoutbuilder:staticlayout-proxy:1.6.0")
}
