import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.railprep.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "railprep.android.application"
            implementationClass = "com.railprep.build.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "railprep.android.library"
            implementationClass = "com.railprep.build.AndroidLibraryConventionPlugin"
        }
        register("androidComposeLibrary") {
            id = "railprep.android.compose.library"
            implementationClass = "com.railprep.build.AndroidComposeLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "railprep.android.feature"
            implementationClass = "com.railprep.build.AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "railprep.jvm.library"
            implementationClass = "com.railprep.build.JvmLibraryConventionPlugin"
        }
    }
}
