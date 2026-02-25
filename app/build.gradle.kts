import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val env = Properties()
val envFile = project.file(".env")
if (envFile.exists()) {
    env.load(envFile.inputStream())
}

android {
    namespace = "com.example.lab5_starter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lab5_starter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Pass the firebase key to the build config or as a resource if needed
        // But the user wants to call it in google-services.json which is processed by the plugin.
        // The google-services plugin doesn't support variable substitution directly in the JSON file.
        // However, we can use a Gradle task to replace the placeholder in the JSON file during build,
        // OR we can use the 'googleServices' configuration in gradle if available, 
        // but it's limited.
        
        // A common trick is to use a placeholder in google-services.json and use a pre-build task to replace it.
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
}

// Task to inject the API key into google-services.json
tasks.register("injectFirebaseKey") {
    val jsonFile = file("google-services.json")
    val envValue = env.getProperty("Firebase_key") ?: ""
    
    doLast {
        if (jsonFile.exists() && envValue.isNotEmpty()) {
            var content = jsonFile.readText()
            // Using a specific placeholder logic or just replacing the known key if it exists
            // To make it robust, we should use a placeholder like "${FIREBASE_KEY}" in the file
            content = content.replace("PLACEHOLDER_FIREBASE_KEY", envValue)
            jsonFile.writeText(content)
        }
    }
}

// Ensure the injection happens before the google-services plugin processes the file
tasks.matching { it.name.contains("process") && it.name.contains("GoogleServices") }.configureEach {
    dependsOn("injectFirebaseKey")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-firestore")

}
