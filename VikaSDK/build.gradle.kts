import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.vika.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "SDK_VERSION", "\"1.0.3\"")
        buildConfigField("String", "BASE_URL", "\"https://vika-ng.ekoptra.com\"")
        buildConfigField("String", "SOCKET_URL", "\"https://vika-ng.ekoptra.com\"")
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {

    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.material3)
    api(libs.androidx.material.icons.extended)
    api(libs.androidx.activity.compose)
    api(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlin
    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines.android)

    // Networking
    api(libs.retrofit)
    api(libs.converter.gson)
    api(libs.okhttp)
    api(libs.logging.interceptor)
    api(libs.socket.io.client)

    // Security
    api(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// GitHub Packages Publishing Configuration
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.vika.sdk"
                artifactId = "vika-sdk"
                version = "1.0.3"

                pom {
                    name.set("VIKA")
                    description.set("Voice-driven navigation SDK for Android")
                    url.set("https://github.com/arafat1419/VIKA")

                    licenses {
                        license {
                            name.set("Proprietary")
                            url.set("https://github.com/arafat1419/VIKA/blob/main/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            id.set("vika")
                            name.set("VIKA Team")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/arafat1419/VIKA")
                credentials {
                    // Read from local.properties (not committed to Git)
                    val localProperties = Properties()
                    val localPropertiesFile = rootProject.file("local.properties")
                    if (localPropertiesFile.exists()) {
                        localPropertiesFile.inputStream().use { stream ->
                            localProperties.load(stream)
                        }
                    }

                    username =
                        localProperties.getProperty("github.user") ?: System.getenv("GITHUB_ACTOR")
                    password =
                        localProperties.getProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}