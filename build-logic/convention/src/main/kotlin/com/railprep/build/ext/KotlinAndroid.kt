package com.railprep.build.ext

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

internal fun VersionCatalog.stringVersion(alias: String): String =
    findVersion(alias).get().requiredVersion

// Bytecode target is Java 17 (set via libs.versions.toml `jvmTarget`). The toolchain that runs
// the build is pinned to the JVM currently running Gradle — i.e. whatever the user launched Gradle
// with — to avoid AGP 8.x's implicit JDK-17 toolchain request when no JDK 17 is installed. Any
// modern JDK (17+) can emit Java 17 bytecode, so this is safe.
private val runningJvmLanguageVersion: JavaLanguageVersion
    get() = JavaLanguageVersion.of(JavaVersion.current().majorVersion)

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    val jvmTargetVersion = libs.stringVersion("jvmTarget")

    commonExtension.apply {
        compileSdk = libs.intVersion("compileSdk")

        defaultConfig {
            minSdk = libs.intVersion("minSdk")
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
            targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
            isCoreLibraryDesugaringEnabled = false
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(runningJvmLanguageVersion)
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
            )
        }
    }

    configureJUnitPlatform()
}

internal fun Project.configureKotlinJvm() {
    val jvmTargetVersion = libs.stringVersion("jvmTarget")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
        targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
        toolchain.languageVersion.set(runningJvmLanguageVersion)
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
            )
        }
    }

    configureJUnitPlatform()
}

private fun Project.configureJUnitPlatform() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
