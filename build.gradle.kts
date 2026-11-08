import java.util.Properties

plugins {
    // Keep in step with Versions.agp of the main build.
    id("com.android.application") version "9.2.1"
}

android {
    namespace = "io.ymusic.android.saf"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.ymusic.android.saf"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // YMusic's release key, from secrets.properties (./decrypt.sh) or env YMUSIC_ANDROID_*: the picker answers only
    // apps signed like itself. Without it the release build is debug-signed.
    val secrets = Properties().apply {
        rootProject.file("secrets.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
    }
    fun secret(env: String, key: String): String? =
        (System.getenv(env) ?: secrets.getProperty(key))?.trim()?.takeIf { it.isNotEmpty() }
    val releaseSigning = secret("YMUSIC_ANDROID_STORE_FILE", "android.signing.storeFile")
        ?.let { rootProject.file(it) }
        ?.takeIf { it.isFile }
        ?.let { store ->
            signingConfigs.create("release") {
                storeFile = store
                storePassword = secret("YMUSIC_ANDROID_STORE_PASSWORD", "android.signing.storePassword")
                keyAlias = secret("YMUSIC_ANDROID_KEY_ALIAS", "android.signing.keyAlias")
                keyPassword = secret("YMUSIC_ANDROID_KEY_PASSWORD", "android.signing.keyPassword") ?: storePassword
            }
        }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = releaseSigning ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}
