plugins {
    alias(libs.plugins.railprep.android.feature)
}

android {
    namespace = "com.railprep.feature.paywall"
}

dependencies {
    implementation(project(":core:core-design"))
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.play.billing.ktx)
}
