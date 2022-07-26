plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization") version("1.7.10")
    id("com.android.library")
}

group = "com.ekdorn.classicdungeon"
version = "0.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

kotlin {
    android()
    mingwX64("desktop") {
        binaries {
            sharedLib {
                baseName = "libnative"
            }
        }
    }
    js(IR) {
        browser()
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("co.touchlab:kermit:1.1.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jsMain by getting
    }
}

android {
    compileSdkVersion(32)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(32)
    }
}
