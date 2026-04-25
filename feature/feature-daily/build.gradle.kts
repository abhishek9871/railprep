plugins {
    alias(libs.plugins.railprep.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.railprep.feature.daily"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-design"))
    implementation(project(":core:core-common"))
    // Post-submit shows the notifications opt-in dialog (D2). Strictly one-way dep.
    implementation(project(":feature:feature-notifications"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.compose.material.icons)
}
