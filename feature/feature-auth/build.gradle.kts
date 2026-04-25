plugins {
    alias(libs.plugins.railprep.android.feature)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.railprep.feature.auth"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-design"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-i18n"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Credential Manager + Google Identity
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)  // Paparazzi uses JUnit 4 @Rule
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
