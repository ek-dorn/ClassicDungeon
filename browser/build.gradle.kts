plugins {
    kotlin("js")
}

group = "com.ekdorn.classicdungeon"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "ClassicDungeon.js"
                cssSupport.enabled = true
            }
        }
    }
}



// Workaround for gradle not building correctly.

tasks.create("copyResources") {
    copy {
        from("../shared/build/processedResources/js/main")
        into("build/processedResources/js/main")
    }
}
tasks["browserDistribution"].doLast {
    tasks["copyResources"]
}
