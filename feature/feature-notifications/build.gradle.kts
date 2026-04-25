plugins {
    alias(libs.plugins.railprep.android.feature)
}

android {
    namespace = "com.railprep.feature.notifications"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-design"))
    implementation(project(":core:core-common"))

    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.compose.material.icons)
}
