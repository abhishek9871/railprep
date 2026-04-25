plugins {
    alias(libs.plugins.railprep.android.compose.library)
}

android {
    namespace = "com.railprep.core.design"
}

dependencies {
    implementation(libs.androidx.compose.material.icons)
}
