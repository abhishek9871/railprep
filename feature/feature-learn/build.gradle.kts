plugins {
    alias(libs.plugins.railprep.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.railprep.feature.learn"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-design"))
    implementation(project(":core:core-common"))

    implementation(libs.kotlinx.serialization.json)

    // YouTube videos render via a plain WebView pointing at youtube.com/iframe_api — no
    // third-party wrapper library. See feature/learn/youtube/YouTubePlayer.kt for the
    // init/baseUrl rationale. androidx.webkit is required to opt the WebView out of the
    // Media Integrity API attestation that YT otherwise checks (and that some modified GMS
    // stacks, e.g. Revanced's gmscore, break for every embed).
    implementation(libs.androidx.webkit)

    // OkHttp for PDF downloads (PDF rendering uses Android's built-in PdfRenderer).
    implementation(libs.okhttp.core)

    // Coil for YouTube thumbnails on list tiles.
    implementation(libs.coil.compose)

    implementation(libs.androidx.compose.material.icons)
}
