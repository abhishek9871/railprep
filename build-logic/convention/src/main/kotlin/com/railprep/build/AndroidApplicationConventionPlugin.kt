package com.railprep.build

import com.android.build.api.dsl.ApplicationExtension
import com.railprep.build.ext.configureKotlinAndroid
import com.railprep.build.ext.libs
import com.railprep.build.ext.loadLocalProperties
import com.railprep.build.ext.stringOrEmpty
import com.railprep.build.ext.stringVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        val props = loadLocalProperties()
        val supabaseUrl = props.stringOrEmpty("SUPABASE_URL")
        val supabaseAnonKey = props.stringOrEmpty("SUPABASE_ANON_KEY")
        val googleWebClientId = props.stringOrEmpty("GOOGLE_WEB_CLIENT_ID")
        val privacyPolicyUrl = props.stringOrEmpty("RAILPREP_PRIVACY_POLICY_URL")
        val termsUrl = props.stringOrEmpty("RAILPREP_TERMS_URL")
        val supportEmail = props.stringOrEmpty("RAILPREP_SUPPORT_EMAIL")

        if (supabaseUrl.isEmpty() || supabaseAnonKey.isEmpty() || googleWebClientId.isEmpty()) {
            logger.lifecycle(
                "[railprep] local.properties missing SUPABASE_URL / SUPABASE_ANON_KEY / GOOGLE_WEB_CLIENT_ID " +
                    "— empty BuildConfig values used; see README for the template.",
            )
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)

            defaultConfig {
                targetSdk = libs.stringVersion("targetSdk").toInt()
                versionCode = 2
                versionName = "1.0.1"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables { useSupportLibrary = true }
            }

            flavorDimensions += "environment"
            productFlavors {
                create("dev") {
                    dimension = "environment"
                    applicationIdSuffix = ".dev"
                    versionNameSuffix = "-dev"
                }
                create("prod") {
                    dimension = "environment"
                }
            }

            signingConfigs {
                create("releaseStub") {
                    val ksPath = props.stringOrEmpty("RP_KEYSTORE_PATH")
                    val ksPassword = props.stringOrEmpty("RP_KEYSTORE_PASSWORD")
                    val kAlias = props.stringOrEmpty("RP_KEY_ALIAS")
                    val kPassword = props.stringOrEmpty("RP_KEY_PASSWORD")
                    if (ksPath.isNotEmpty()) {
                        storeFile = rootProject.file(ksPath)
                        storePassword = ksPassword
                        keyAlias = kAlias
                        keyPassword = kPassword
                    }
                }
            }

            buildTypes {
                getByName("debug") {
                    isMinifyEnabled = false
                }
                getByName("release") {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                    if (props.stringOrEmpty("RP_KEYSTORE_PATH").isNotEmpty()) {
                        signingConfig = signingConfigs.getByName("releaseStub")
                    }
                }
            }

            buildFeatures {
                buildConfig = true
            }

            defaultConfig {
                buildConfigField("String", "SUPABASE_URL",          "\"$supabaseUrl\"")
                buildConfigField("String", "SUPABASE_ANON_KEY",     "\"$supabaseAnonKey\"")
                buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",  "\"$googleWebClientId\"")
                buildConfigField("String", "PRIVACY_POLICY_URL",    "\"$privacyPolicyUrl\"")
                buildConfigField("String", "TERMS_URL",             "\"$termsUrl\"")
                buildConfigField("String", "SUPPORT_EMAIL",         "\"$supportEmail\"")
            }

            packaging {
                resources {
                    excludes += setOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "/META-INF/licenses/**",
                        "/META-INF/versions/**",
                        "/META-INF/proguard/**",
                        "META-INF/INDEX.LIST",
                    )
                }
            }
        }
    }
}
