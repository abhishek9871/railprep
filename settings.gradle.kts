pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "railprep"

include(":app")

include(":core:core-design")
include(":core:core-network")
include(":core:core-database")
include(":core:core-common")
include(":core:core-analytics")
include(":core:core-i18n")

include(":feature:feature-auth")
include(":feature:feature-home")
include(":feature:feature-learn")
include(":feature:feature-tests")
include(":feature:feature-feed")
include(":feature:feature-daily")
include(":feature:feature-notifications")
include(":feature:feature-doubts")
include(":feature:feature-profile")
include(":feature:feature-paywall")
include(":feature:feature-offline")

include(":data:data-repository")
include(":data:data-remote")

include(":domain")
