plugins {
    alias(libs.plugins.railprep.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.railprep"

    defaultConfig {
        applicationId = "com.railprep"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:core-design"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-analytics"))
    implementation(project(":core:core-i18n"))

    // Feature modules
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-learn"))
    implementation(project(":feature:feature-tests"))
    implementation(project(":feature:feature-feed"))
    implementation(project(":feature:feature-daily"))
    implementation(project(":feature:feature-notifications"))
    implementation(project(":feature:feature-doubts"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-paywall"))
    implementation(project(":feature:feature-offline"))

    // Data + domain
    implementation(project(":data:data-repository"))
    implementation(project(":data:data-remote"))
    implementation(project(":domain"))

    // Compose BOM (app pulls its own; the convention plugin is only for libraries)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
