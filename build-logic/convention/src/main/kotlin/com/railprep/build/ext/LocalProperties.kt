package com.railprep.build.ext

import org.gradle.api.Project
import java.io.File
import java.util.Properties

internal fun Project.loadLocalProperties(): Properties {
    val props = Properties()
    val file = File(rootDir, "local.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

internal fun Properties.stringOrEmpty(key: String): String = getProperty(key, "").trim()
