// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle.android)
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.sqldelight)
    }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.github.ben-manes.versions") version libs.versions.versionsPlugin.get()
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean",Delete::class) {
    delete(rootProject.buildDir)
}