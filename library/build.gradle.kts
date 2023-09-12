plugins {
    java
    id("maven-publish")
    kotlin("jvm")
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://www.jitpack.io" ) }
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.mcal"
            artifactId = "apkparser"
            version = "1.2.5"

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}
