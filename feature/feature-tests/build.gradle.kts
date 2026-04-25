plugins {
    alias(libs.plugins.railprep.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.railprep.feature.tests"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-design"))
    implementation(project(":core:core-common"))
    // PYQ library-link screen reuses feature-learn's PdfViewer + PdfCache
    // (NCERT pattern from Phase 2.5). Direction of dep: tests → learn, never the reverse.
    implementation(project(":feature:feature-learn"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Local Room mirror — absorbs answer writes during network drops and rehydrates the
    // player on cold start when there's an IN_PROGRESS attempt outstanding.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // WorkManager — expedited one-time job schedules an auto-submit at deadline as the
    // belt-and-braces safety net (primary auto-submit still happens in-VM while the UI
    // is alive; WM covers process death before the deadline).
    implementation(libs.androidx.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.androidx.compose.material.icons)
}
